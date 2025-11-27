export interface Ticket {
  id: string;
  ticketNumber: string;
  subject: string;
  description?: string;
  status: TicketStatus;
  priority: TicketPriority;
  type: TicketType;
  channel: TicketChannel;

  projectId: string;
  projectName: string;
  projectKey: string;

  categoryId?: string;
  categoryName?: string;

  requesterId: string;
  requesterName: string;
  requesterEmail: string;
  requesterAvatarUrl?: string;

  assigneeId?: string;
  assigneeName?: string;
  assigneeEmail?: string;
  assigneeAvatarUrl?: string;

  teamId?: string;
  teamName?: string;

  dueDate?: string;
  firstResponseAt?: string;
  resolvedAt?: string;
  closedAt?: string;
  reopenedCount: number;

  csatRating?: number;
  csatComment?: string;

  tags: string[];

  commentCount: number;
  attachmentCount: number;

  createdAt: string;
  updatedAt?: string;
  createdBy: string;
}

export enum TicketStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  PENDING = 'PENDING',
  ON_HOLD = 'ON_HOLD',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED',
  CANCELLED = 'CANCELLED'
}

export enum TicketPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

export enum TicketType {
  QUESTION = 'QUESTION',
  INCIDENT = 'INCIDENT',
  PROBLEM = 'PROBLEM',
  FEATURE_REQUEST = 'FEATURE_REQUEST',
  TASK = 'TASK'
}

export enum TicketChannel {
  WEB = 'WEB',
  EMAIL = 'EMAIL',
  TELEGRAM = 'TELEGRAM',
  WHATSAPP = 'WHATSAPP',
  PHONE = 'PHONE',
  CHAT = 'CHAT',
  API = 'API'
}

export interface TicketComment {
  id: string;
  ticketId: string;
  authorId: string;
  authorName: string;
  authorEmail: string;
  authorAvatarUrl?: string;
  content: string;
  contentHtml?: string;
  type: CommentType;
  channel?: TicketChannel;
  attachments: TicketAttachment[];
  createdAt: string;
  updatedAt?: string;
}

export enum CommentType {
  PUBLIC = 'PUBLIC',
  INTERNAL = 'INTERNAL',
  SYSTEM = 'SYSTEM'
}

export interface TicketAttachment {
  id: string;
  ticketId: string;
  commentId?: string;
  uploadedById: string;
  uploadedByName: string;
  fileName: string;
  originalName: string;
  contentType: string;
  fileSize: number;
  downloadUrl: string;
  thumbnailUrl?: string;
  createdAt: string;
}

export interface CreateTicketRequest {
  subject: string;
  description?: string;
  projectId: string;
  categoryId?: string;
  assigneeId?: string;
  teamId?: string;
  priority?: TicketPriority;
  type?: TicketType;
  channel?: TicketChannel;
  dueDate?: string;
  tags?: string[];
}

export interface UpdateTicketRequest {
  subject?: string;
  description?: string;
  status?: TicketStatus;
  priority?: TicketPriority;
  type?: TicketType;
  categoryId?: string;
  assigneeId?: string;
  teamId?: string;
  dueDate?: string;
  tags?: string[];
}

export interface TicketFilter {
  search?: string;
  statuses?: TicketStatus[];
  priorities?: TicketPriority[];
  types?: TicketType[];
  channels?: TicketChannel[];
  projectIds?: string[];
  assigneeIds?: string[];
  requesterIds?: string[];
  teamIds?: string[];
  unassigned?: boolean;
  overdue?: boolean;
  createdFrom?: string;
  createdTo?: string;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}

export interface CreateCommentRequest {
  content: string;
  contentHtml?: string;
  type?: CommentType;
}
