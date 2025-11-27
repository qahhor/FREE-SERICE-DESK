export interface Ticket {
  id: string;
  number: string;
  subject: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  type: TicketType;
  channel: TicketChannel;
  categoryId?: string;
  categoryName?: string;
  projectId?: string;
  projectName?: string;
  customerId: string;
  customerName: string;
  customerEmail: string;
  assignedAgentId?: string;
  assignedAgentName?: string;
  teamId?: string;
  teamName?: string;
  tags?: string[];
  slaStatus?: SlaStatus;
  firstResponseAt?: Date;
  resolvedAt?: Date;
  closedAt?: Date;
  rating?: number;
  feedback?: string;
  createdAt: Date;
  updatedAt: Date;
  comments?: TicketComment[];
  attachments?: TicketAttachment[];
}

export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'PENDING' | 'ON_HOLD' | 'RESOLVED' | 'CLOSED' | 'CANCELLED';

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export type TicketType = 'QUESTION' | 'INCIDENT' | 'PROBLEM' | 'FEATURE_REQUEST' | 'TASK';

export type TicketChannel = 'WEB' | 'EMAIL' | 'TELEGRAM' | 'WHATSAPP' | 'PHONE' | 'CHAT' | 'API';

export type SlaStatus = 'ON_TRACK' | 'AT_RISK' | 'BREACHED';

export interface TicketComment {
  id: string;
  ticketId: string;
  content: string;
  isInternal: boolean;
  authorId: string;
  authorName: string;
  authorType: 'CUSTOMER' | 'AGENT' | 'SYSTEM';
  attachments?: TicketAttachment[];
  createdAt: Date;
}

export interface TicketAttachment {
  id: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  url: string;
  uploadedAt: Date;
}

export interface CreateTicketRequest {
  subject: string;
  description: string;
  priority?: TicketPriority;
  type?: TicketType;
  categoryId?: string;
  attachments?: File[];
}

export interface AddCommentRequest {
  content: string;
  attachments?: File[];
}

export interface TicketRatingRequest {
  rating: number;
  feedback?: string;
}

export interface TicketFilter {
  status?: TicketStatus[];
  priority?: TicketPriority[];
  type?: TicketType[];
  dateFrom?: Date;
  dateTo?: Date;
  search?: string;
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  parentId?: string;
  articleCount?: number;
}
