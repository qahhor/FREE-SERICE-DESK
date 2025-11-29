import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';
import { TicketsService } from '../services/tickets.service';
import { TicketPriority } from '../models/ticket.model';

@Component({
  selector: 'app-ticket-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    LayoutComponent,
    LoadingSpinnerComponent
  ],
  templateUrl: './ticket-create.component.html',
  styleUrl: './ticket-create.component.scss'
})
export class TicketCreateComponent {
  private fb = inject(FormBuilder);
  private ticketsService = inject(TicketsService);
  private router = inject(Router);

  ticketForm: FormGroup;
  loading = false;
  error = '';

  priorities = Object.values(TicketPriority);

  constructor() {
    this.ticketForm = this.fb.group({
      title: ['', [Validators.required]],
      description: ['', [Validators.required]],
      priority: [TicketPriority.MEDIUM, [Validators.required]],
      category: [''],
      customerEmail: ['', [Validators.email]],
      tags: ['']
    });
  }

  onSubmit(): void {
    if (this.ticketForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    const formValue = this.ticketForm.value;
    const tags = formValue.tags ? formValue.tags.split(',').map((t: string) => t.trim()) : [];

    const ticketData = {
      ...formValue,
      tags
    };

    this.ticketsService.createTicket(ticketData).subscribe({
      next: (ticket) => {
        this.router.navigate(['/tickets', ticket.id]);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.message || 'Failed to create ticket';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/tickets']);
  }
}
