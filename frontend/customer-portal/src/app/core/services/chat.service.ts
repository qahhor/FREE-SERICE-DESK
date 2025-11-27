import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  ChatSession,
  ChatMessage,
  StartChatRequest,
  SendMessageRequest,
  EndChatRequest,
  TypingIndicator,
  QueueInfo,
  ChatEvent
} from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService implements OnDestroy {
  private stompClient: Client | null = null;
  private subscriptions: StompSubscription[] = [];

  private connectionStatus = new BehaviorSubject<boolean>(false);
  connectionStatus$ = this.connectionStatus.asObservable();

  private currentSession = new BehaviorSubject<ChatSession | null>(null);
  currentSession$ = this.currentSession.asObservable();

  private messages = new BehaviorSubject<ChatMessage[]>([]);
  messages$ = this.messages.asObservable();

  private typingIndicator = new Subject<TypingIndicator>();
  typingIndicator$ = this.typingIndicator.asObservable();

  private chatEvents = new Subject<ChatEvent>();
  chatEvents$ = this.chatEvents.asObservable();

  private queueInfo = new BehaviorSubject<QueueInfo | null>(null);
  queueInfo$ = this.queueInfo.asObservable();

  private visitorId: string;
  private baseUrl = `${environment.apiUrl}/api/v1/livechat`;
  private wsUrl = environment.wsUrl;

  constructor(private http: HttpClient) {
    this.visitorId = this.getOrCreateVisitorId();
  }

  private getOrCreateVisitorId(): string {
    let visitorId = localStorage.getItem('visitor_id');
    if (!visitorId) {
      visitorId = 'visitor_' + Math.random().toString(36).substr(2, 9) + Date.now().toString(36);
      localStorage.setItem('visitor_id', visitorId);
    }
    return visitorId;
  }

  connect(): void {
    if (this.stompClient?.active) {
      return;
    }

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(`${this.wsUrl}/ws/livechat`),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      connectHeaders: {
        'X-Visitor-Id': this.visitorId
      }
    });

    this.stompClient.onConnect = () => {
      console.log('Chat WebSocket connected');
      this.connectionStatus.next(true);
    };

    this.stompClient.onDisconnect = () => {
      console.log('Chat WebSocket disconnected');
      this.connectionStatus.next(false);
    };

    this.stompClient.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
    };

    this.stompClient.activate();
  }

  disconnect(): void {
    this.unsubscribeAll();
    this.stompClient?.deactivate();
    this.connectionStatus.next(false);
  }

  startChat(request: Partial<StartChatRequest>): Observable<ApiResponse<ChatSession>> {
    const fullRequest: StartChatRequest = {
      projectId: environment.projectId || 'default',
      visitorId: this.visitorId,
      visitorName: request.visitorName,
      visitorEmail: request.visitorEmail,
      visitorPhone: request.visitorPhone,
      department: request.department,
      pageUrl: window.location.href,
      pageTitle: document.title,
      initialMessage: request.initialMessage
    };

    return new Observable(observer => {
      this.http.post<ApiResponse<ChatSession>>(`${this.baseUrl}/sessions/start`, fullRequest)
        .subscribe({
          next: (response) => {
            if (response.success && response.data) {
              this.currentSession.next(response.data);
              this.messages.next(response.data.recentMessages || []);
              this.subscribeToSession(response.data.id);
            }
            observer.next(response);
            observer.complete();
          },
          error: (error) => observer.error(error)
        });
    });
  }

  private subscribeToSession(sessionId: string): void {
    if (!this.stompClient?.active) {
      console.warn('WebSocket not connected, cannot subscribe');
      return;
    }

    // Subscribe to messages
    const messageSub = this.stompClient.subscribe(`/topic/chat/${sessionId}/messages`, (message: IMessage) => {
      const chatMessage: ChatMessage = JSON.parse(message.body);
      const currentMessages = this.messages.value;
      this.messages.next([...currentMessages, chatMessage]);
    });
    this.subscriptions.push(messageSub);

    // Subscribe to typing indicators
    const typingSub = this.stompClient.subscribe(`/topic/chat/${sessionId}/typing`, (message: IMessage) => {
      const indicator: TypingIndicator = JSON.parse(message.body);
      this.typingIndicator.next(indicator);
    });
    this.subscriptions.push(typingSub);

    // Subscribe to chat events
    const eventsSub = this.stompClient.subscribe(`/topic/chat/${sessionId}/events`, (message: IMessage) => {
      const event: ChatEvent = JSON.parse(message.body);
      this.chatEvents.next(event);

      // Update session on certain events
      if (event.type === 'CHAT_ASSIGNED' || event.type === 'CHAT_ENDED') {
        if (event.payload) {
          this.currentSession.next(event.payload);
        }
      }
    });
    this.subscriptions.push(eventsSub);

    // Subscribe to queue updates
    const queueSub = this.stompClient.subscribe(`/topic/chat/${sessionId}/queue`, (message: IMessage) => {
      const queue: QueueInfo = JSON.parse(message.body);
      this.queueInfo.next(queue);
    });
    this.subscriptions.push(queueSub);
  }

  sendMessage(content: string, messageType: string = 'TEXT'): Observable<ApiResponse<ChatMessage>> {
    const session = this.currentSession.value;
    if (!session) {
      throw new Error('No active chat session');
    }

    const request: SendMessageRequest = {
      sessionId: session.id,
      visitorId: this.visitorId,
      content,
      messageType: messageType as any
    };

    return this.http.post<ApiResponse<ChatMessage>>(
      `${this.baseUrl}/sessions/${session.id}/messages/visitor`,
      request
    );
  }

  sendTyping(isTyping: boolean): void {
    const session = this.currentSession.value;
    if (!session || !this.stompClient?.active) {
      return;
    }

    const indicator: TypingIndicator = {
      sessionId: session.id,
      visitorId: this.visitorId,
      isTyping,
      senderType: 'VISITOR'
    };

    this.stompClient.publish({
      destination: `/app/chat/${session.id}/typing`,
      body: JSON.stringify(indicator)
    });
  }

  endChat(rating?: number, feedback?: string): Observable<ApiResponse<ChatSession>> {
    const session = this.currentSession.value;
    if (!session) {
      throw new Error('No active chat session');
    }

    const request: EndChatRequest = {
      sessionId: session.id,
      endedBy: 'VISITOR',
      rating,
      feedback
    };

    return new Observable(observer => {
      this.http.post<ApiResponse<ChatSession>>(`${this.baseUrl}/sessions/${session.id}/end`, request)
        .subscribe({
          next: (response) => {
            if (response.success) {
              this.unsubscribeAll();
              this.currentSession.next(null);
              this.messages.next([]);
              this.queueInfo.next(null);
            }
            observer.next(response);
            observer.complete();
          },
          error: (error) => observer.error(error)
        });
    });
  }

  markAsRead(): void {
    const session = this.currentSession.value;
    if (!session) {
      return;
    }

    this.http.post(`${this.baseUrl}/sessions/${session.id}/read?visitorId=${this.visitorId}`, {})
      .subscribe();
  }

  getQueueInfo(): Observable<ApiResponse<QueueInfo>> {
    const session = this.currentSession.value;
    if (!session) {
      throw new Error('No active chat session');
    }

    return this.http.get<ApiResponse<QueueInfo>>(`${this.baseUrl}/sessions/${session.id}/queue`);
  }

  loadExistingSession(): Observable<ApiResponse<ChatSession>> {
    return this.http.get<ApiResponse<ChatSession>>(`${this.baseUrl}/sessions/visitor/${this.visitorId}`);
  }

  private unsubscribeAll(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.subscriptions = [];
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
