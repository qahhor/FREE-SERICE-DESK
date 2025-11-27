import './styles/widget.scss';

interface WidgetConfig {
  projectId: string;
  apiUrl?: string;
  position?: 'bottom-right' | 'bottom-left';
  primaryColor?: string;
  greeting?: string;
  locale?: string;
  zIndex?: number;
}

interface Message {
  id: string;
  content: string;
  sender: 'user' | 'agent' | 'bot';
  timestamp: Date;
  attachments?: Attachment[];
}

interface Attachment {
  name: string;
  url: string;
  type: string;
  size: number;
}

interface Conversation {
  id: string;
  status: 'active' | 'waiting' | 'closed';
  messages: Message[];
}

class ServiceDeskWidget {
  private config: WidgetConfig;
  private container: HTMLElement | null = null;
  private isOpen: boolean = false;
  private conversation: Conversation | null = null;
  private socket: WebSocket | null = null;
  private visitorId: string;
  private unreadCount: number = 0;

  private translations: Record<string, Record<string, string>> = {
    en: {
      greeting: 'Hi! How can we help you today?',
      placeholder: 'Type your message...',
      send: 'Send',
      connecting: 'Connecting...',
      connected: 'Connected',
      offline: 'We are offline',
      emailPlaceholder: 'Enter your email',
      namePlaceholder: 'Enter your name',
      startChat: 'Start Chat',
      endChat: 'End Chat',
      minimize: 'Minimize',
      attachFile: 'Attach file',
      typing: 'Agent is typing...',
      powered: 'Powered by ServiceDesk'
    },
    ru: {
      greeting: 'Привет! Чем можем помочь?',
      placeholder: 'Введите сообщение...',
      send: 'Отправить',
      connecting: 'Подключение...',
      connected: 'Подключено',
      offline: 'Мы офлайн',
      emailPlaceholder: 'Введите email',
      namePlaceholder: 'Введите имя',
      startChat: 'Начать чат',
      endChat: 'Завершить чат',
      minimize: 'Свернуть',
      attachFile: 'Прикрепить файл',
      typing: 'Агент печатает...',
      powered: 'Работает на ServiceDesk'
    },
    uz: {
      greeting: 'Salom! Qanday yordam bera olamiz?',
      placeholder: 'Xabar yozing...',
      send: 'Yuborish',
      connecting: 'Ulanmoqda...',
      connected: 'Ulandi',
      offline: 'Biz oflaynmiz',
      emailPlaceholder: 'Email kiriting',
      namePlaceholder: 'Ismingizni kiriting',
      startChat: 'Chatni boshlash',
      endChat: 'Chatni tugatish',
      minimize: 'Kichraytirish',
      attachFile: 'Fayl biriktirish',
      typing: 'Agent yozmoqda...',
      powered: 'ServiceDesk asosida'
    }
  };

  constructor(config: WidgetConfig) {
    this.config = {
      apiUrl: 'https://api.servicedesk.local',
      position: 'bottom-right',
      primaryColor: '#1976d2',
      locale: 'en',
      zIndex: 999999,
      ...config
    };

    this.visitorId = this.getOrCreateVisitorId();
    this.init();
  }

  private getOrCreateVisitorId(): string {
    let visitorId = localStorage.getItem('sd_visitor_id');
    if (!visitorId) {
      visitorId = 'v_' + Math.random().toString(36).substr(2, 9) + Date.now().toString(36);
      localStorage.setItem('sd_visitor_id', visitorId);
    }
    return visitorId;
  }

  private t(key: string): string {
    const locale = this.config.locale || 'en';
    return this.translations[locale]?.[key] || this.translations['en'][key] || key;
  }

  private init(): void {
    this.createContainer();
    this.render();
    this.loadConversation();
  }

  private createContainer(): void {
    this.container = document.createElement('div');
    this.container.id = 'servicedesk-widget';
    this.container.style.cssText = `
      position: fixed;
      ${this.config.position === 'bottom-left' ? 'left: 20px;' : 'right: 20px;'}
      bottom: 20px;
      z-index: ${this.config.zIndex};
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    `;
    document.body.appendChild(this.container);
  }

  private render(): void {
    if (!this.container) return;

    const primaryColor = this.config.primaryColor;

    this.container.innerHTML = `
      <style>
        #sd-launcher {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          background: ${primaryColor};
          border: none;
          cursor: pointer;
          box-shadow: 0 4px 12px rgba(0,0,0,0.15);
          display: flex;
          align-items: center;
          justify-content: center;
          transition: transform 0.2s, box-shadow 0.2s;
          position: relative;
        }
        #sd-launcher:hover {
          transform: scale(1.05);
          box-shadow: 0 6px 16px rgba(0,0,0,0.2);
        }
        #sd-launcher svg {
          width: 28px;
          height: 28px;
          fill: white;
        }
        #sd-badge {
          position: absolute;
          top: -5px;
          right: -5px;
          background: #f44336;
          color: white;
          border-radius: 50%;
          width: 22px;
          height: 22px;
          font-size: 12px;
          display: ${this.unreadCount > 0 ? 'flex' : 'none'};
          align-items: center;
          justify-content: center;
          font-weight: bold;
        }
        #sd-chat-window {
          display: ${this.isOpen ? 'flex' : 'none'};
          flex-direction: column;
          width: 380px;
          height: 520px;
          background: white;
          border-radius: 16px;
          box-shadow: 0 8px 32px rgba(0,0,0,0.15);
          overflow: hidden;
          margin-bottom: 12px;
        }
        #sd-header {
          background: ${primaryColor};
          color: white;
          padding: 16px 20px;
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        #sd-header h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
        }
        #sd-header-actions button {
          background: transparent;
          border: none;
          color: white;
          cursor: pointer;
          padding: 4px 8px;
          opacity: 0.8;
        }
        #sd-header-actions button:hover {
          opacity: 1;
        }
        #sd-messages {
          flex: 1;
          overflow-y: auto;
          padding: 16px;
          background: #f5f5f5;
        }
        .sd-message {
          margin-bottom: 12px;
          display: flex;
          flex-direction: column;
        }
        .sd-message.user {
          align-items: flex-end;
        }
        .sd-message.agent, .sd-message.bot {
          align-items: flex-start;
        }
        .sd-message-bubble {
          max-width: 80%;
          padding: 10px 14px;
          border-radius: 16px;
          font-size: 14px;
          line-height: 1.4;
        }
        .sd-message.user .sd-message-bubble {
          background: ${primaryColor};
          color: white;
          border-bottom-right-radius: 4px;
        }
        .sd-message.agent .sd-message-bubble,
        .sd-message.bot .sd-message-bubble {
          background: white;
          color: #333;
          border-bottom-left-radius: 4px;
          box-shadow: 0 1px 2px rgba(0,0,0,0.1);
        }
        .sd-message-time {
          font-size: 11px;
          color: #999;
          margin-top: 4px;
        }
        #sd-input-area {
          padding: 12px 16px;
          background: white;
          border-top: 1px solid #eee;
          display: flex;
          gap: 8px;
          align-items: center;
        }
        #sd-input {
          flex: 1;
          border: 1px solid #ddd;
          border-radius: 20px;
          padding: 10px 16px;
          font-size: 14px;
          outline: none;
          transition: border-color 0.2s;
        }
        #sd-input:focus {
          border-color: ${primaryColor};
        }
        #sd-send-btn {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: ${primaryColor};
          border: none;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: background 0.2s;
        }
        #sd-send-btn:hover {
          background: ${this.adjustColor(primaryColor!, -20)};
        }
        #sd-send-btn:disabled {
          background: #ccc;
          cursor: not-allowed;
        }
        #sd-send-btn svg {
          width: 20px;
          height: 20px;
          fill: white;
        }
        #sd-powered {
          text-align: center;
          padding: 8px;
          font-size: 11px;
          color: #999;
          background: #fafafa;
        }
        #sd-pre-chat {
          flex: 1;
          display: flex;
          flex-direction: column;
          justify-content: center;
          padding: 24px;
          background: #f5f5f5;
        }
        #sd-pre-chat h4 {
          margin: 0 0 20px 0;
          text-align: center;
          color: #333;
        }
        #sd-pre-chat input {
          width: 100%;
          padding: 12px 16px;
          border: 1px solid #ddd;
          border-radius: 8px;
          font-size: 14px;
          margin-bottom: 12px;
          box-sizing: border-box;
        }
        #sd-start-btn {
          width: 100%;
          padding: 12px;
          background: ${primaryColor};
          color: white;
          border: none;
          border-radius: 8px;
          font-size: 14px;
          font-weight: 600;
          cursor: pointer;
          transition: background 0.2s;
        }
        #sd-start-btn:hover {
          background: ${this.adjustColor(primaryColor!, -20)};
        }
        #sd-typing {
          display: none;
          padding: 8px 16px;
          font-size: 12px;
          color: #666;
          font-style: italic;
        }
        @media (max-width: 480px) {
          #sd-chat-window {
            width: 100vw;
            height: 100vh;
            border-radius: 0;
            position: fixed;
            top: 0;
            left: 0;
            margin: 0;
          }
        }
      </style>

      <div id="sd-chat-window">
        <div id="sd-header">
          <h3>${this.config.greeting || this.t('greeting')}</h3>
          <div id="sd-header-actions">
            <button onclick="ServiceDeskWidget.instance.toggle()" title="${this.t('minimize')}">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                <path d="M19 13H5v-2h14v2z"/>
              </svg>
            </button>
          </div>
        </div>

        ${this.conversation ? this.renderMessages() : this.renderPreChat()}

        <div id="sd-powered">${this.t('powered')}</div>
      </div>

      <button id="sd-launcher" onclick="ServiceDeskWidget.instance.toggle()">
        <svg viewBox="0 0 24 24">
          <path d="${this.isOpen ? 'M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z' : 'M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z'}"/>
        </svg>
        <span id="sd-badge">${this.unreadCount}</span>
      </button>
    `;

    // Bind events
    this.bindEvents();
  }

  private renderPreChat(): string {
    return `
      <div id="sd-pre-chat">
        <h4>${this.t('greeting')}</h4>
        <input type="text" id="sd-name" placeholder="${this.t('namePlaceholder')}" />
        <input type="email" id="sd-email" placeholder="${this.t('emailPlaceholder')}" />
        <button id="sd-start-btn" onclick="ServiceDeskWidget.instance.startConversation()">
          ${this.t('startChat')}
        </button>
      </div>
    `;
  }

  private renderMessages(): string {
    const messages = this.conversation?.messages || [];
    return `
      <div id="sd-messages">
        ${messages.map(msg => `
          <div class="sd-message ${msg.sender}">
            <div class="sd-message-bubble">${this.escapeHtml(msg.content)}</div>
            <span class="sd-message-time">${this.formatTime(msg.timestamp)}</span>
          </div>
        `).join('')}
      </div>
      <div id="sd-typing">${this.t('typing')}</div>
      <div id="sd-input-area">
        <input type="text" id="sd-input" placeholder="${this.t('placeholder')}"
               onkeypress="if(event.key==='Enter')ServiceDeskWidget.instance.sendMessage()"/>
        <button id="sd-send-btn" onclick="ServiceDeskWidget.instance.sendMessage()">
          <svg viewBox="0 0 24 24">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
          </svg>
        </button>
      </div>
    `;
  }

  private bindEvents(): void {
    // Auto-scroll messages
    const messagesEl = document.getElementById('sd-messages');
    if (messagesEl) {
      messagesEl.scrollTop = messagesEl.scrollHeight;
    }
  }

  public toggle(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.unreadCount = 0;
    }
    this.render();
  }

  public open(): void {
    this.isOpen = true;
    this.render();
  }

  public close(): void {
    this.isOpen = false;
    this.render();
  }

  public async startConversation(): Promise<void> {
    const nameEl = document.getElementById('sd-name') as HTMLInputElement;
    const emailEl = document.getElementById('sd-email') as HTMLInputElement;

    const name = nameEl?.value?.trim();
    const email = emailEl?.value?.trim();

    if (!name || !email) {
      alert('Please fill in all fields');
      return;
    }

    try {
      const response = await fetch(`${this.config.apiUrl}/api/v1/widget/conversations`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          projectId: this.config.projectId,
          visitorId: this.visitorId,
          visitorName: name,
          visitorEmail: email,
          locale: this.config.locale
        })
      });

      const data = await response.json();
      this.conversation = {
        id: data.data.id,
        status: 'active',
        messages: []
      };

      localStorage.setItem('sd_conversation_id', this.conversation.id);
      this.connectWebSocket();
      this.render();

      // Add greeting message
      this.addMessage({
        id: 'greeting',
        content: this.config.greeting || this.t('greeting'),
        sender: 'bot',
        timestamp: new Date()
      });

    } catch (error) {
      console.error('Failed to start conversation:', error);
    }
  }

  public async sendMessage(): Promise<void> {
    const inputEl = document.getElementById('sd-input') as HTMLInputElement;
    const content = inputEl?.value?.trim();

    if (!content || !this.conversation) return;

    const message: Message = {
      id: 'msg_' + Date.now(),
      content,
      sender: 'user',
      timestamp: new Date()
    };

    this.addMessage(message);
    inputEl.value = '';

    try {
      await fetch(`${this.config.apiUrl}/api/v1/widget/conversations/${this.conversation.id}/messages`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          content,
          visitorId: this.visitorId
        })
      });
    } catch (error) {
      console.error('Failed to send message:', error);
    }
  }

  private addMessage(message: Message): void {
    if (!this.conversation) return;
    this.conversation.messages.push(message);
    this.render();

    if (!this.isOpen && message.sender !== 'user') {
      this.unreadCount++;
      this.updateBadge();
    }
  }

  private updateBadge(): void {
    const badge = document.getElementById('sd-badge');
    if (badge) {
      badge.textContent = String(this.unreadCount);
      badge.style.display = this.unreadCount > 0 ? 'flex' : 'none';
    }
  }

  private connectWebSocket(): void {
    if (!this.conversation) return;

    const wsUrl = this.config.apiUrl?.replace('http', 'ws') + '/ws/widget';
    this.socket = new WebSocket(`${wsUrl}?conversationId=${this.conversation.id}&visitorId=${this.visitorId}`);

    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data);

      if (data.type === 'message') {
        this.addMessage({
          id: data.id,
          content: data.content,
          sender: data.sender,
          timestamp: new Date(data.timestamp)
        });
      } else if (data.type === 'typing') {
        this.showTypingIndicator(data.isTyping);
      }
    };

    this.socket.onclose = () => {
      setTimeout(() => this.connectWebSocket(), 3000);
    };
  }

  private showTypingIndicator(show: boolean): void {
    const typingEl = document.getElementById('sd-typing');
    if (typingEl) {
      typingEl.style.display = show ? 'block' : 'none';
    }
  }

  private async loadConversation(): Promise<void> {
    const conversationId = localStorage.getItem('sd_conversation_id');
    if (!conversationId) return;

    try {
      const response = await fetch(
        `${this.config.apiUrl}/api/v1/widget/conversations/${conversationId}?visitorId=${this.visitorId}`
      );

      if (response.ok) {
        const data = await response.json();
        this.conversation = {
          id: data.data.id,
          status: data.data.status,
          messages: data.data.messages || []
        };
        this.connectWebSocket();
        this.render();
      }
    } catch (error) {
      console.error('Failed to load conversation:', error);
    }
  }

  private formatTime(date: Date): string {
    return new Date(date).toLocaleTimeString(this.config.locale, {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  private escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  private adjustColor(color: string, amount: number): string {
    const clamp = (num: number) => Math.min(255, Math.max(0, num));
    const hex = color.replace('#', '');
    const r = clamp(parseInt(hex.substr(0, 2), 16) + amount);
    const g = clamp(parseInt(hex.substr(2, 2), 16) + amount);
    const b = clamp(parseInt(hex.substr(4, 2), 16) + amount);
    return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
  }

  public destroy(): void {
    if (this.socket) {
      this.socket.close();
    }
    if (this.container) {
      this.container.remove();
    }
  }

  static instance: ServiceDeskWidget;
}

// Auto-initialize if config is present
declare global {
  interface Window {
    ServiceDeskWidgetConfig?: WidgetConfig;
    ServiceDeskWidget: typeof ServiceDeskWidget;
  }
}

if (typeof window !== 'undefined') {
  window.ServiceDeskWidget = ServiceDeskWidget;

  if (window.ServiceDeskWidgetConfig) {
    ServiceDeskWidget.instance = new ServiceDeskWidget(window.ServiceDeskWidgetConfig);
  }
}

export default ServiceDeskWidget;
