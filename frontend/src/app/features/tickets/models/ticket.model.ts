export interface Ticket {
  id: string;
  title: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  category?: string;
  customerId: string;
  customerName?: string;
  customerEmail?: string;
  assignedAgentId?: string;
  assignedAgentName?: string;
  channelType?: string;
  channelId?: string;
  tags?: string[];
  attachments?: string[];
  createdAt: Date;
  updatedAt: Date;
  resolvedAt?: Date;
  closedAt?: Date;
  slaDeadline?: Date;
  comments?: TicketComment[];
}

export enum TicketStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  WAITING_FOR_CUSTOMER = 'WAITING_FOR_CUSTOMER',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

export enum TicketPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

export interface TicketComment {
  id: string;
  ticketId: string;
  authorId: string;
  authorName: string;
  content: string;
  isInternal: boolean;
  attachments?: string[];
  createdAt: Date;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
  category?: string;
  customerId?: string;
  customerEmail?: string;
  tags?: string[];
}

export interface UpdateTicketRequest {
  title?: string;
  description?: string;
  status?: TicketStatus;
  priority?: TicketPriority;
  category?: string;
  assignedAgentId?: string;
  tags?: string[];
}
