import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { TicketPriority, TicketType, TicketChannel } from '../../../core/models/ticket.model';
import * as TicketActions from '../store/ticket.actions';

@Component({
  selector: 'app-ticket-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatSnackBarModule,
    TranslateModule
  ],
  template: `
    <div class="ticket-create-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ 'ticket.create' | translate }}</mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="ticketForm" (ngSubmit)="onSubmit()">
            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>{{ 'ticket.subject' | translate }}</mat-label>
                <input matInput formControlName="subject" [placeholder]="'ticket.subject' | translate">
                <mat-error *ngIf="ticketForm.get('subject')?.hasError('required')">
                  {{ 'validation.required' | translate }}
                </mat-error>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>{{ 'ticket.description' | translate }}</mat-label>
                <textarea matInput formControlName="description" rows="6"
                          [placeholder]="'ticket.description' | translate"></textarea>
                <mat-error *ngIf="ticketForm.get('description')?.hasError('required')">
                  {{ 'validation.required' | translate }}
                </mat-error>
              </mat-form-field>
            </div>

            <div class="form-row two-columns">
              <mat-form-field appearance="outline">
                <mat-label>{{ 'ticket.priority' | translate }}</mat-label>
                <mat-select formControlName="priority">
                  <mat-option *ngFor="let priority of priorities" [value]="priority">
                    {{ priority }}
                  </mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>{{ 'ticket.type' | translate }}</mat-label>
                <mat-select formControlName="type">
                  <mat-option *ngFor="let type of types" [value]="type">
                    {{ type }}
                  </mat-option>
                </mat-select>
              </mat-form-field>
            </div>

            <div class="form-row two-columns">
              <mat-form-field appearance="outline">
                <mat-label>{{ 'ticket.channel' | translate }}</mat-label>
                <mat-select formControlName="channel">
                  <mat-option *ngFor="let channel of channels" [value]="channel">
                    {{ channel }}
                  </mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>{{ 'ticket.category' | translate }}</mat-label>
                <mat-select formControlName="categoryId">
                  <mat-option [value]="null">-- Select --</mat-option>
                </mat-select>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>{{ 'ticket.requester' | translate }}</mat-label>
                <input matInput formControlName="requesterEmail" type="email"
                       [placeholder]="'auth.email' | translate">
              </mat-form-field>
            </div>

            <div class="form-actions">
              <button mat-button type="button" (click)="onCancel()">
                {{ 'common.cancel' | translate }}
              </button>
              <button mat-raised-button color="primary" type="submit"
                      [disabled]="ticketForm.invalid || isSubmitting">
                {{ 'common.create' | translate }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .ticket-create-container {
      padding: 24px;
      max-width: 800px;
      margin: 0 auto;
    }

    .form-row {
      margin-bottom: 16px;
    }

    .form-row.two-columns {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }

    .full-width {
      width: 100%;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
    }

    mat-form-field {
      width: 100%;
    }
  `]
})
export class TicketCreateComponent implements OnInit {
  ticketForm!: FormGroup;
  isSubmitting = false;

  priorities = Object.values(TicketPriority);
  types = Object.values(TicketType);
  channels = Object.values(TicketChannel);

  constructor(
    private fb: FormBuilder,
    private store: Store,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.ticketForm = this.fb.group({
      subject: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.required]],
      priority: [TicketPriority.MEDIUM, Validators.required],
      type: [TicketType.QUESTION, Validators.required],
      channel: [TicketChannel.WEB, Validators.required],
      categoryId: [null],
      requesterEmail: ['', [Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.ticketForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    const request = this.ticketForm.value;

    this.store.dispatch(TicketActions.createTicket({ request }));

    this.snackBar.open(
      this.translate.instant('common.success'),
      this.translate.instant('common.close'),
      { duration: 3000 }
    );

    this.router.navigate(['/tickets']);
  }

  onCancel(): void {
    this.router.navigate(['/tickets']);
  }
}
