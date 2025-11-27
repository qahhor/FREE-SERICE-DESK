import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatBadgeModule } from '@angular/material/badge';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil, debounceTime } from 'rxjs';
import { ChatService } from '../../../core/services/chat.service';
import { ChatSession, ChatMessage, TypingIndicator, QueueInfo } from '../../../core/models/chat.model';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatBadgeModule,
    TranslateModule
  ],
  template: `
    <div class="chat-widget" [class.open]="isOpen">
      <!-- Chat button -->
      <button
        *ngIf="!isOpen"
        class="chat-button"
        (click)="toggleChat()"
        [matBadge]="unreadCount"
        [matBadgeHidden]="unreadCount === 0"
        matBadgeColor="warn">
        <mat-icon>chat</mat-icon>
      </button>

      <!-- Chat window -->
      <div class="chat-window" *ngIf="isOpen">
        <!-- Header -->
        <div class="chat-header">
          <div class="header-info">
            <mat-icon>support_agent</mat-icon>
            <div class="header-text">
              <span class="title">{{ 'chat.title' | translate }}</span>
              <span class="status" [class.online]="isConnected">
                {{ isConnected ? ('chat.online' | translate) : ('chat.offline' | translate) }}
              </span>
            </div>
          </div>
          <button mat-icon-button (click)="toggleChat()">
            <mat-icon>close</mat-icon>
          </button>
        </div>

        <!-- Content -->
        <div class="chat-content">
          <!-- Pre-chat form -->
          <div class="pre-chat" *ngIf="!session && !isStarting">
            <h3>{{ 'chat.start_conversation' | translate }}</h3>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'chat.name' | translate }}</mat-label>
              <input matInput [(ngModel)]="visitorName" [placeholder]="'chat.name_placeholder' | translate">
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'chat.email' | translate }}</mat-label>
              <input matInput type="email" [(ngModel)]="visitorEmail" [placeholder]="'chat.email_placeholder' | translate">
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'chat.message' | translate }}</mat-label>
              <textarea matInput [(ngModel)]="initialMessage" rows="3" [placeholder]="'chat.message_placeholder' | translate"></textarea>
            </mat-form-field>
            <button mat-raised-button color="primary" (click)="startChat()" [disabled]="!initialMessage">
              {{ 'chat.start_chat' | translate }}
            </button>
          </div>

          <!-- Loading -->
          <div class="loading" *ngIf="isStarting">
            <mat-spinner diameter="40"></mat-spinner>
            <p>{{ 'chat.connecting' | translate }}</p>
          </div>

          <!-- Queue -->
          <div class="queue-info" *ngIf="session && session.status === 'WAITING' && queueInfo">
            <mat-icon>schedule</mat-icon>
            <p>{{ 'chat.queue_position' | translate: { position: queueInfo.position } }}</p>
            <small>{{ 'chat.estimated_wait' | translate: { time: queueInfo.estimatedWaitTime } }}</small>
          </div>

          <!-- Messages -->
          <div class="messages" #messagesContainer *ngIf="session">
            <div
              *ngFor="let message of messages"
              class="message"
              [class.visitor]="message.senderType === 'VISITOR'"
              [class.agent]="message.senderType === 'AGENT'"
              [class.system]="message.senderType === 'SYSTEM'">
              <div class="message-content">
                <span class="sender" *ngIf="message.senderType !== 'VISITOR'">{{ message.senderName }}</span>
                <p>{{ message.content }}</p>
                <span class="time">{{ message.timestamp | date:'shortTime' }}</span>
              </div>
            </div>

            <!-- Typing indicator -->
            <div class="typing-indicator" *ngIf="isAgentTyping">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="chat-input" *ngIf="session && session.status === 'ACTIVE'">
          <mat-form-field appearance="outline" class="message-input">
            <input
              matInput
              [(ngModel)]="messageText"
              (keyup.enter)="sendMessage()"
              (input)="onTyping()"
              [placeholder]="'chat.type_message' | translate">
          </mat-form-field>
          <button mat-icon-button color="primary" (click)="sendMessage()" [disabled]="!messageText.trim()">
            <mat-icon>send</mat-icon>
          </button>
        </div>

        <!-- Ended chat -->
        <div class="chat-ended" *ngIf="session && session.status === 'CLOSED'">
          <mat-icon>check_circle</mat-icon>
          <p>{{ 'chat.ended' | translate }}</p>
          <div class="rating" *ngIf="!hasRated">
            <p>{{ 'chat.rate_conversation' | translate }}</p>
            <div class="stars">
              <button *ngFor="let star of [1,2,3,4,5]" mat-icon-button (click)="rateChat(star)">
                <mat-icon>{{ star <= rating ? 'star' : 'star_border' }}</mat-icon>
              </button>
            </div>
          </div>
          <button mat-button (click)="startNewChat()">{{ 'chat.new_chat' | translate }}</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .chat-widget {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 1000;
    }

    .chat-button {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: #673ab7;
      color: white;
      border: none;
      cursor: pointer;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform 0.2s, box-shadow 0.2s;

      &:hover {
        transform: scale(1.1);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
      }

      mat-icon {
        font-size: 28px;
        width: 28px;
        height: 28px;
      }
    }

    .chat-window {
      width: 380px;
      height: 550px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .chat-header {
      background: #673ab7;
      color: white;
      padding: 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .header-info {
        display: flex;
        align-items: center;
        gap: 12px;

        mat-icon {
          font-size: 32px;
          width: 32px;
          height: 32px;
        }
      }

      .header-text {
        display: flex;
        flex-direction: column;

        .title {
          font-weight: 500;
          font-size: 16px;
        }

        .status {
          font-size: 12px;
          opacity: 0.8;

          &.online::before {
            content: '';
            display: inline-block;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #4caf50;
            margin-right: 6px;
          }
        }
      }
    }

    .chat-content {
      flex: 1;
      overflow-y: auto;
      padding: 16px;
    }

    .pre-chat {
      h3 {
        margin: 0 0 16px 0;
        color: #333;
      }

      .full-width {
        width: 100%;
      }

      button {
        width: 100%;
        margin-top: 8px;
      }
    }

    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      gap: 16px;
      color: #666;
    }

    .queue-info {
      text-align: center;
      padding: 24px;
      background: #f5f5f5;
      border-radius: 8px;

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: #673ab7;
      }

      p {
        margin: 8px 0 4px;
        font-weight: 500;
      }

      small {
        color: #666;
      }
    }

    .messages {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .message {
        max-width: 85%;

        &.visitor {
          align-self: flex-end;

          .message-content {
            background: #673ab7;
            color: white;
            border-radius: 16px 16px 4px 16px;
          }
        }

        &.agent {
          align-self: flex-start;

          .message-content {
            background: #f0f0f0;
            border-radius: 16px 16px 16px 4px;
          }
        }

        &.system {
          align-self: center;

          .message-content {
            background: transparent;
            color: #666;
            font-size: 12px;
            text-align: center;
          }
        }
      }

      .message-content {
        padding: 10px 14px;

        .sender {
          display: block;
          font-size: 11px;
          font-weight: 500;
          margin-bottom: 4px;
          opacity: 0.8;
        }

        p {
          margin: 0;
          word-break: break-word;
        }

        .time {
          display: block;
          font-size: 10px;
          opacity: 0.6;
          margin-top: 4px;
          text-align: right;
        }
      }
    }

    .typing-indicator {
      display: flex;
      gap: 4px;
      padding: 12px;
      background: #f0f0f0;
      border-radius: 16px;
      width: fit-content;

      span {
        width: 8px;
        height: 8px;
        background: #999;
        border-radius: 50%;
        animation: typing 1.4s infinite;

        &:nth-child(2) { animation-delay: 0.2s; }
        &:nth-child(3) { animation-delay: 0.4s; }
      }
    }

    @keyframes typing {
      0%, 60%, 100% { transform: translateY(0); }
      30% { transform: translateY(-4px); }
    }

    .chat-input {
      padding: 12px;
      border-top: 1px solid #eee;
      display: flex;
      gap: 8px;
      align-items: center;

      .message-input {
        flex: 1;
        margin: 0;
      }
    }

    .chat-ended {
      text-align: center;
      padding: 24px;

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: #4caf50;
      }

      .rating {
        margin: 16px 0;

        .stars button {
          color: #ffc107;
        }
      }
    }

    @media (max-width: 480px) {
      .chat-widget.open {
        bottom: 0;
        right: 0;
        left: 0;
      }

      .chat-window {
        width: 100%;
        height: 100vh;
        border-radius: 0;
      }
    }
  `]
})
export class ChatWidgetComponent implements OnInit, OnDestroy {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  isOpen = false;
  isConnected = false;
  isStarting = false;
  isAgentTyping = false;
  hasRated = false;

  session: ChatSession | null = null;
  messages: ChatMessage[] = [];
  queueInfo: QueueInfo | null = null;
  unreadCount = 0;
  rating = 0;

  visitorName = '';
  visitorEmail = '';
  initialMessage = '';
  messageText = '';

  private destroy$ = new Subject<void>();
  private typingSubject = new Subject<void>();

  constructor(private chatService: ChatService) {}

  ngOnInit(): void {
    // Subscribe to chat service observables
    this.chatService.connectionStatus$
      .pipe(takeUntil(this.destroy$))
      .subscribe(status => this.isConnected = status);

    this.chatService.currentSession$
      .pipe(takeUntil(this.destroy$))
      .subscribe(session => {
        this.session = session;
        if (session) this.isStarting = false;
      });

    this.chatService.messages$
      .pipe(takeUntil(this.destroy$))
      .subscribe(messages => {
        this.messages = messages;
        this.scrollToBottom();
        if (!this.isOpen) {
          this.unreadCount = messages.filter(m => m.senderType !== 'VISITOR' && !m.isRead).length;
        }
      });

    this.chatService.typingIndicator$
      .pipe(takeUntil(this.destroy$))
      .subscribe(indicator => {
        if (indicator.senderType === 'AGENT') {
          this.isAgentTyping = indicator.isTyping;
        }
      });

    this.chatService.queueInfo$
      .pipe(takeUntil(this.destroy$))
      .subscribe(info => this.queueInfo = info);

    // Debounce typing events
    this.typingSubject
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe(() => this.chatService.sendTyping(true));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;

    if (this.isOpen) {
      this.unreadCount = 0;
      this.chatService.connect();
      this.chatService.markAsRead();
    }
  }

  startChat(): void {
    if (!this.initialMessage.trim()) return;

    this.isStarting = true;
    this.chatService.startChat({
      visitorName: this.visitorName || undefined,
      visitorEmail: this.visitorEmail || undefined,
      initialMessage: this.initialMessage
    }).subscribe({
      error: () => this.isStarting = false
    });
  }

  sendMessage(): void {
    if (!this.messageText.trim()) return;

    this.chatService.sendMessage(this.messageText).subscribe(() => {
      this.messageText = '';
      this.chatService.sendTyping(false);
    });
  }

  onTyping(): void {
    this.typingSubject.next();
  }

  rateChat(stars: number): void {
    this.rating = stars;
    this.chatService.endChat(stars).subscribe(() => {
      this.hasRated = true;
    });
  }

  startNewChat(): void {
    this.session = null;
    this.messages = [];
    this.hasRated = false;
    this.rating = 0;
    this.initialMessage = '';
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesContainer) {
        const el = this.messagesContainer.nativeElement;
        el.scrollTop = el.scrollHeight;
      }
    }, 100);
  }
}
