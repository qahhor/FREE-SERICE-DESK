import { Injectable, inject } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { io, Socket } from 'socket.io-client';
import { environment } from '@environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private authService = inject(AuthService);
  private socket: Socket | null = null;
  private messageSubject = new Subject<any>();

  /**
   * Connect to WebSocket server
   */
  connect(): void {
    if (this.socket?.connected) {
      return;
    }

    const token = this.authService.getToken();
    if (!token) {
      console.error('Cannot connect to WebSocket: No auth token');
      return;
    }

    this.socket = io(environment.wsUrl, {
      auth: { token },
      transports: ['websocket']
    });

    this.socket.on('connect', () => {
      console.log('WebSocket connected');
    });

    this.socket.on('disconnect', () => {
      console.log('WebSocket disconnected');
    });

    this.socket.on('error', (error: any) => {
      console.error('WebSocket error:', error);
    });

    // Generic message handler
    this.socket.onAny((event, data) => {
      this.messageSubject.next({ event, data });
    });
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): void {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }

  /**
   * Listen to specific event
   */
  on(event: string): Observable<any> {
    return new Observable(observer => {
      if (!this.socket) {
        this.connect();
      }

      this.socket?.on(event, (data: any) => {
        observer.next(data);
      });

      return () => {
        this.socket?.off(event);
      };
    });
  }

  /**
   * Emit event to server
   */
  emit(event: string, data: any): void {
    if (!this.socket?.connected) {
      this.connect();
    }

    this.socket?.emit(event, data);
  }

  /**
   * Get all messages observable
   */
  getMessages(): Observable<any> {
    return this.messageSubject.asObservable();
  }

  /**
   * Check if connected
   */
  isConnected(): boolean {
    return this.socket?.connected || false;
  }
}
