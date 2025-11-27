import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { TicketService } from '../../../core/services/ticket.service';
import { Category } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-create',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TranslateModule
  ],
  template: `
    <div class="create-ticket-container">
      <div class="header">
        <a mat-icon-button routerLink="/tickets">
          <mat-icon>arrow_back</mat-icon>
        </a>
        <h1>{{ 'tickets.new_ticket' | translate }}</h1>
      </div>

      <mat-card>
        <mat-card-content>
          <form [formGroup]="ticketForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'tickets.subject' | translate }}</mat-label>
              <input matInput formControlName="subject" [placeholder]="'tickets.subject_placeholder' | translate">
              <mat-error *ngIf="ticketForm.get('subject')?.hasError('required')">
                {{ 'validation.subject_required' | translate }}
              </mat-error>
            </mat-form-field>

            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>{{ 'tickets.category' | translate }}</mat-label>
                <mat-select formControlName="categoryId">
                  <mat-option *ngFor="let cat of categories" [value]="cat.id">
                    {{ cat.name }}
                  </mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>{{ 'tickets.priority' | translate }}</mat-label>
                <mat-select formControlName="priority">
                  <mat-option value="LOW">{{ 'ticket.priority.low' | translate }}</mat-option>
                  <mat-option value="MEDIUM">{{ 'ticket.priority.medium' | translate }}</mat-option>
                  <mat-option value="HIGH">{{ 'ticket.priority.high' | translate }}</mat-option>
                  <mat-option value="URGENT">{{ 'ticket.priority.urgent' | translate }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>{{ 'tickets.type' | translate }}</mat-label>
                <mat-select formControlName="type">
                  <mat-option value="QUESTION">{{ 'ticket.type.question' | translate }}</mat-option>
                  <mat-option value="INCIDENT">{{ 'ticket.type.incident' | translate }}</mat-option>
                  <mat-option value="PROBLEM">{{ 'ticket.type.problem' | translate }}</mat-option>
                  <mat-option value="FEATURE_REQUEST">{{ 'ticket.type.feature_request' | translate }}</mat-option>
                </mat-select>
              </mat-form-field>
            </div>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'tickets.description' | translate }}</mat-label>
              <textarea matInput formControlName="description" rows="8" [placeholder]="'tickets.description_placeholder' | translate"></textarea>
              <mat-error *ngIf="ticketForm.get('description')?.hasError('required')">
                {{ 'validation.description_required' | translate }}
              </mat-error>
              <mat-hint align="end">{{ ticketForm.get('description')?.value?.length || 0 }} / 5000</mat-hint>
            </mat-form-field>

            <!-- File upload -->
            <div class="file-upload">
              <input type="file" #fileInput multiple (change)="onFilesSelected($event)" hidden>
              <button mat-stroked-button type="button" (click)="fileInput.click()">
                <mat-icon>attach_file</mat-icon>
                {{ 'tickets.attach_files' | translate }}
              </button>
              <div class="file-list" *ngIf="selectedFiles.length > 0">
                <div class="file-item" *ngFor="let file of selectedFiles; let i = index">
                  <mat-icon>description</mat-icon>
                  <span>{{ file.name }}</span>
                  <span class="file-size">({{ formatFileSize(file.size) }})</span>
                  <button mat-icon-button type="button" (click)="removeFile(i)">
                    <mat-icon>close</mat-icon>
                  </button>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button mat-button type="button" routerLink="/tickets">
                {{ 'common.cancel' | translate }}
              </button>
              <button mat-raised-button color="primary" type="submit" [disabled]="isSubmitting || ticketForm.invalid">
                <mat-spinner *ngIf="isSubmitting" diameter="20"></mat-spinner>
                <span *ngIf="!isSubmitting">{{ 'tickets.submit' | translate }}</span>
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .create-ticket-container {
      max-width: 800px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;

      h1 {
        margin: 0;
        color: #333;
      }
    }

    .full-width {
      width: 100%;
    }

    .form-row {
      display: flex;
      gap: 16px;

      mat-form-field {
        flex: 1;
      }
    }

    .file-upload {
      margin: 16px 0;

      .file-list {
        margin-top: 12px;
      }

      .file-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px;
        background: #f5f5f5;
        border-radius: 4px;
        margin-bottom: 8px;

        mat-icon {
          color: #666;
        }

        span {
          flex: 1;
        }

        .file-size {
          color: #666;
          font-size: 12px;
        }
      }
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 16px;
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid #eee;

      button[type="submit"] {
        min-width: 120px;

        mat-spinner {
          display: inline-block;
          margin-right: 8px;
        }
      }
    }

    @media (max-width: 768px) {
      .form-row {
        flex-direction: column;
      }
    }
  `]
})
export class TicketCreateComponent implements OnInit {
  ticketForm: FormGroup;
  categories: Category[] = [];
  selectedFiles: File[] = [];
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private ticketService: TicketService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.ticketForm = this.fb.group({
      subject: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.required, Validators.maxLength(5000)]],
      categoryId: [''],
      priority: ['MEDIUM'],
      type: ['QUESTION']
    });
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  private loadCategories(): void {
    this.ticketService.getCategories().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.categories = response.data;
        }
      }
    });
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const newFiles = Array.from(input.files);
      this.selectedFiles = [...this.selectedFiles, ...newFiles];
    }
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  onSubmit(): void {
    if (this.ticketForm.invalid) return;

    this.isSubmitting = true;

    const request = {
      ...this.ticketForm.value,
      attachments: this.selectedFiles
    };

    this.ticketService.createTicket(request).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.snackBar.open('Ticket created successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/tickets', response.data.id]);
        }
      },
      error: () => {
        this.isSubmitting = false;
        this.snackBar.open('Failed to create ticket. Please try again.', 'Close', { duration: 5000 });
      },
      complete: () => {
        this.isSubmitting = false;
      }
    });
  }
}
