export interface ChatSession {
  id: string;
  visitorId: string;
  visitorName?: string;
  assignedAgentId?: string;
  assignedAgentName?: string;
  status: ChatSessionStatus;
  queuePosition?: number;
  startedAt: Date;
  endedAt?: Date;
  lastActivityAt: Date;
  messageCount: number;
  rating?: number;
  feedback?: string;
  recentMessages?: ChatMessage[];
}

export type ChatSessionStatus = 'WAITING' | 'ACTIVE' | 'ON_HOLD' | 'TRANSFERRED' | 'CLOSED' | 'ABANDONED' | 'MISSED';

export interface ChatMessage {
  id: string;
  sessionId: string;
  senderType: SenderType;
  senderId?: string;
  senderName?: string;
  senderAvatar?: string;
  content: string;
  messageType: MessageType;
  attachmentUrl?: string;
  attachmentName?: string;
  isRead: boolean;
  readAt?: Date;
  timestamp: Date;
}

export type SenderType = 'VISITOR' | 'AGENT' | 'SYSTEM' | 'BOT';

export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'AUDIO' | 'VIDEO' | 'CARD' | 'BUTTONS' | 'QUICK_REPLIES' | 'FORM' | 'SYSTEM';

export interface StartChatRequest {
  projectId: string;
  visitorId: string;
  visitorName?: string;
  visitorEmail?: string;
  visitorPhone?: string;
  department?: string;
  pageUrl?: string;
  pageTitle?: string;
  initialMessage?: string;
}

export interface SendMessageRequest {
  sessionId: string;
  visitorId: string;
  content: string;
  messageType?: MessageType;
  attachmentUrl?: string;
  attachmentName?: string;
}

export interface TypingIndicator {
  sessionId: string;
  visitorId?: string;
  agentId?: string;
  isTyping: boolean;
  senderType: SenderType;
}

export interface EndChatRequest {
  sessionId: string;
  endedBy: string;
  rating?: number;
  feedback?: string;
}

export interface QueueInfo {
  sessionId: string;
  position: number;
  estimatedWaitTime: number;
  department?: string;
}

export interface ChatEvent {
  type: ChatEventType;
  sessionId: string;
  visitorId?: string;
  agentId?: string;
  payload?: any;
  timestamp: Date;
}

export type ChatEventType =
  | 'CHAT_STARTED'
  | 'CHAT_ASSIGNED'
  | 'CHAT_TRANSFERRED'
  | 'CHAT_ENDED'
  | 'MESSAGE_RECEIVED'
  | 'MESSAGE_SENT'
  | 'MESSAGE_READ'
  | 'TYPING_STARTED'
  | 'TYPING_STOPPED'
  | 'AGENT_JOINED'
  | 'AGENT_LEFT'
  | 'VISITOR_CONNECTED'
  | 'VISITOR_DISCONNECTED'
  | 'QUEUE_POSITION_UPDATED';
