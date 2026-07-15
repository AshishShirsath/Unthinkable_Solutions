import { Component, inject, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ChatRoomResponse, ChatMessageResponse } from '../../core/models/api.models';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="chat-container">
      <!-- Left side: chat rooms list -->
      <div class="rooms-sidebar" [class.mobile-hidden]="selectedRoomId && isMobileView">
        <div class="sidebar-header">
          <h3>Conversations</h3>
          <p class="muted">{{ chatRooms.length }} active chat{{ chatRooms.length === 1 ? '' : 's' }}</p>
        </div>
        <div class="rooms-list">
          <div 
            *ngFor="let room of chatRooms" 
            class="room-item" 
            [class.active]="selectedRoom?.id === room.id"
            (click)="selectRoom(room)"
          >
            <div class="room-avatar">
              {{ getParticipantName(room)[0] }}
            </div>
            <div class="room-details">
              <div class="room-meta">
                <span class="participant-name">{{ getParticipantName(room) }}</span>
                <span class="room-time" *ngIf="room.lastMessage">{{ formatTime(room.lastMessage.sentAt) }}</span>
              </div>
              <span class="listing-title">Listing: {{ room.listingTitle }}</span>
              <p class="last-message" *ngIf="room.lastMessage">
                {{ room.lastMessage.content }}
              </p>
              <p class="last-message muted italic" *ngIf="!room.lastMessage">
                No messages yet. Say hi!
              </p>
            </div>
          </div>
          <div *ngIf="!chatRooms.length" class="empty-rooms">
            <p class="muted">No conversations yet.</p>
            <p class="small text-center">Chat rooms appear here once interest requests are accepted.</p>
          </div>
        </div>
      </div>

      <!-- Right side: message history and input -->
      <div class="chat-window" [class.mobile-hidden]="!selectedRoomId && isMobileView">
        <div *ngIf="selectedRoom" class="active-chat">
          <!-- Chat header -->
          <div class="chat-header">
            <button class="back-btn" (click)="goBackToRooms()" *ngIf="isMobileView">← Back</button>
            <div class="chat-header-details">
              <h4>{{ getParticipantName(selectedRoom) }}</h4>
              <a [routerLink]="['/listings', selectedRoom.listingId]" class="listing-link">
                🏠 View Listing: {{ selectedRoom.listingTitle }}
              </a>
            </div>
          </div>

          <!-- Chat messages area -->
          <div class="messages-area" #scrollContainer>
            <div class="messages-list">
              <div 
                *ngFor="let msg of messages; let i = index" 
                class="message-wrapper"
                [class.sent]="isMyMessage(msg)"
                [class.received]="!isMyMessage(msg)"
              >
                <!-- Show date separator if different day -->
                <div class="date-separator" *ngIf="shouldShowDateSeparator(i)">
                  <span>{{ formatDate(msg.sentAt) }}</span>
                </div>

                <div class="message-bubble" [class.sent]="isMyMessage(msg)" [class.received]="!isMyMessage(msg)">
                  <p class="message-content">{{ msg.content }}</p>
                  <span class="message-time">{{ formatTime(msg.sentAt) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Chat footer input -->
          <div class="chat-footer">
            <form (ngSubmit)="sendMessage()" class="input-form">
              <input 
                type="text" 
                name="messageText" 
                [(ngModel)]="newMessageContent" 
                placeholder="Type a message..." 
                autocomplete="off"
                required
              />
              <button type="submit" class="send-btn" [disabled]="!newMessageContent.trim() || sending">
                <span *ngIf="!sending">Send</span>
                <span *ngIf="sending" class="spinner"></span>
              </button>
            </form>
          </div>
        </div>

        <div *ngIf="!selectedRoom" class="no-chat-selected">
          <div class="empty-state-content">
            <span class="empty-state-icon">💬</span>
            <h4>Your Chat Inbox</h4>
            <p class="muted">Select a conversation from the sidebar to start messaging your flatmate / owner.</p>
          </div>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .chat-container {
      display: grid;
      grid-template-columns: 350px 1fr;
      height: calc(100vh - 120px);
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 24px;
      overflow: hidden;
      box-shadow: 0 16px 40px rgba(15,23,42,0.05);
    }

    /* Sidebar styles */
    .rooms-sidebar {
      border-right: 1px solid #e2e8f0;
      display: flex;
      flex-direction: column;
      background: #f8fafc;
      overflow: hidden;
    }
    .sidebar-header {
      padding: 1.25rem;
      border-bottom: 1px solid #e2e8f0;
      background: white;
    }
    .sidebar-header h3 {
      margin: 0 0 0.25rem;
      font-size: 1.2rem;
      color: #0f172a;
    }
    .rooms-list {
      flex: 1;
      overflow-y: auto;
      padding: 0.75rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .room-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem;
      border-radius: 14px;
      cursor: pointer;
      transition: all 0.2s;
      background: white;
      border: 1px solid transparent;
    }
    .room-item:hover {
      background: #f1f5f9;
    }
    .room-item.active {
      background: #eff6ff;
      border-color: #bfdbfe;
    }
    .room-avatar {
      width: 44px;
      height: 44px;
      background: #2563eb;
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 1.1rem;
      flex-shrink: 0;
      text-transform: uppercase;
      box-shadow: 0 4px 10px rgba(37,99,235,0.15);
    }
    .room-details {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
    }
    .room-meta {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      margin-bottom: 0.2rem;
    }
    .participant-name {
      font-weight: 600;
      color: #0f172a;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .room-time {
      font-size: 0.75rem;
      color: #64748b;
    }
    .listing-title {
      font-size: 0.8rem;
      color: #2563eb;
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-bottom: 0.15rem;
    }
    .last-message {
      font-size: 0.82rem;
      color: #64748b;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin: 0;
    }
    .italic { font-style: italic; }
    .empty-rooms {
      text-align: center;
      padding: 2rem 1rem;
    }
    .small { font-size: 0.8rem; }

    /* Chat window styles */
    .chat-window {
      display: flex;
      flex-direction: column;
      background: white;
      height: 100%;
      overflow: hidden;
    }
    .active-chat {
      display: flex;
      flex-direction: column;
      height: 100%;
      overflow: hidden;
    }
    .chat-header {
      padding: 1rem 1.5rem;
      border-bottom: 1px solid #e2e8f0;
      display: flex;
      align-items: center;
      gap: 1rem;
      background: white;
      z-index: 10;
      box-shadow: 0 4px 20px rgba(15,23,42,0.02);
    }
    .back-btn {
      border: none;
      background: transparent;
      font-weight: 600;
      color: #2563eb;
      cursor: pointer;
      font-size: 1rem;
    }
    .chat-header-details {
      display: flex;
      flex-direction: column;
    }
    .chat-header-details h4 {
      margin: 0 0 0.15rem;
      font-size: 1.1rem;
      color: #0f172a;
    }
    .listing-link {
      font-size: 0.85rem;
      color: #2563eb;
      text-decoration: none;
      font-weight: 500;
    }
    .listing-link:hover {
      text-decoration: underline;
    }

    /* Messages area styles */
    .messages-area {
      flex: 1;
      overflow-y: auto;
      padding: 1.5rem;
      background: #f8fafc;
    }
    .messages-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .message-wrapper {
      display: flex;
      flex-direction: column;
      width: 100%;
    }
    .message-wrapper.sent {
      align-items: flex-end;
    }
    .message-wrapper.received {
      align-items: flex-start;
    }
    .message-bubble {
      max-width: 65%;
      padding: 0.75rem 1rem;
      border-radius: 18px;
      position: relative;
      box-shadow: 0 2px 8px rgba(15,23,42,0.03);
      display: flex;
      flex-direction: column;
      gap: 0.2rem;
    }
    .message-bubble.sent {
      background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
      color: white;
      border-bottom-right-radius: 4px;
    }
    .message-bubble.received {
      background: white;
      color: #0f172a;
      border-bottom-left-radius: 4px;
      border: 1px solid #e2e8f0;
    }
    .message-content {
      margin: 0;
      font-size: 0.92rem;
      line-height: 1.4;
      word-break: break-word;
      white-space: pre-wrap;
    }
    .message-time {
      font-size: 0.7rem;
      align-self: flex-end;
    }
    .message-bubble.sent .message-time {
      color: rgba(255,255,255,0.7);
    }
    .message-bubble.received .message-time {
      color: #64748b;
    }

    /* Date Separator */
    .date-separator {
      width: 100%;
      text-align: center;
      margin: 0.5rem 0 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .date-separator::before, .date-separator::after {
      content: '';
      flex: 1;
      border-bottom: 1px solid #e2e8f0;
    }
    .date-separator span {
      background: #f1f5f9;
      color: #64748b;
      padding: 0.25rem 0.6rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 500;
      margin: 0 0.75rem;
    }

    /* Input Footer */
    .chat-footer {
      padding: 1rem 1.5rem;
      border-top: 1px solid #e2e8f0;
      background: white;
    }
    .input-form {
      display: flex;
      gap: 0.75rem;
      align-items: center;
    }
    .input-form input {
      flex: 1;
      border: 1px solid #cbd5e1;
      border-radius: 12px;
      padding: 0.75rem 1rem;
      font-size: 0.95rem;
      outline: none;
      transition: border-color 0.2s;
    }
    .input-form input:focus {
      border-color: #2563eb;
    }
    .send-btn {
      background: #2563eb;
      color: white;
      border: none;
      padding: 0.75rem 1.25rem;
      border-radius: 12px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
      min-width: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .send-btn:hover:not(:disabled) {
      background: #1d4ed8;
    }
    .send-btn:disabled {
      background: #cbd5e1;
      cursor: not-allowed;
    }

    /* Empty state */
    .no-chat-selected {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f8fafc;
      padding: 2rem;
    }
    .empty-state-content {
      text-align: center;
      max-width: 320px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
    }
    .empty-state-icon {
      font-size: 3.5rem;
      margin-bottom: 0.5rem;
    }
    .no-chat-selected h4 {
      margin: 0;
      color: #0f172a;
      font-size: 1.2rem;
    }
    .muted {
      color: #64748b;
      margin: 0;
      font-size: 0.9rem;
      line-height: 1.4;
    }

    /* Spinner */
    .spinner {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(255,255,255,0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    /* Mobile Responsive */
    @media (max-width: 768px) {
      .chat-container {
        grid-template-columns: 1fr;
        height: calc(100vh - 140px);
      }
      .mobile-hidden {
        display: none !important;
      }
    }
  `]
})
export class ChatPageComponent implements OnInit, OnDestroy, AfterViewChecked {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private toast = inject(ToastService);

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  chatRooms: ChatRoomResponse[] = [];
  selectedRoom: ChatRoomResponse | null = null;
  selectedRoomId: number | null = null;
  messages: ChatMessageResponse[] = [];
  newMessageContent = '';
  sending = false;
  currentUserId: number | null = null;

  isMobileView = false;
  private resizeListener!: () => void;

  private pollIntervalId: any;
  private roomsPollIntervalId: any;
  private shouldScroll = false;

  ngOnInit() {
    this.currentUserId = this.auth.currentUser()?.userId ?? null;
    this.checkViewportSize();
    
    // Add window resize listener
    this.resizeListener = () => this.checkViewportSize();
    window.addEventListener('resize', this.resizeListener);

    // Initial load
    this.loadRooms(() => {
      // Check if roomId is provided in path/query params
      this.route.paramMap.subscribe(params => {
        const idStr = params.get('roomId');
        if (idStr) {
          const rId = parseInt(idStr, 10);
          this.activateRoomById(rId);
        } else {
          // Check query parameters as fallback
          this.route.queryParamMap.subscribe(qParams => {
            const qIdStr = qParams.get('roomId');
            if (qIdStr) {
              const rId = parseInt(qIdStr, 10);
              this.activateRoomById(rId);
            }
          });
        }
      });
    });

    // Start background polling for rooms (every 8 seconds)
    this.roomsPollIntervalId = setInterval(() => this.loadRooms(), 8000);
  }

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  ngOnDestroy() {
    this.clearMessagePolling();
    if (this.roomsPollIntervalId) {
      clearInterval(this.roomsPollIntervalId);
    }
    if (this.resizeListener) {
      window.removeEventListener('resize', this.resizeListener);
    }
  }

  private checkViewportSize() {
    this.isMobileView = window.innerWidth <= 768;
  }

  loadRooms(callback?: () => void) {
    this.api.getChatRooms().subscribe({
      next: response => {
        this.chatRooms = response.data ?? [];
        if (callback) callback();
      },
      error: () => undefined
    });
  }

  activateRoomById(roomId: number) {
    const found = this.chatRooms.find(r => r.id === roomId);
    if (found) {
      this.selectRoom(found);
    } else {
      // If not in the local rooms list yet, let's load the messages if we can
      // and wait for rooms list to update or pull rooms again.
      this.selectedRoomId = roomId;
      this.loadMessages(roomId);
      this.startMessagePolling(roomId);
    }
  }

  selectRoom(room: ChatRoomResponse) {
    this.selectedRoom = room;
    this.selectedRoomId = room.id;
    this.newMessageContent = '';
    
    // Update route URL without full reload (for bookmarks / user sharing)
    this.router.navigate(['/chat', room.id], { replaceUrl: true });

    // Fetch messages immediately
    this.loadMessages(room.id, true);
    
    // Start polling messages for this active room
    this.startMessagePolling(room.id);
  }

  goBackToRooms() {
    this.selectedRoom = null;
    this.selectedRoomId = null;
    this.clearMessagePolling();
    this.router.navigate(['/chat'], { replaceUrl: true });
  }

  loadMessages(roomId: number, isInitial = false) {
    this.api.getChatMessages(roomId).subscribe({
      next: response => {
        const msgs = response.data ?? [];
        
        // Only update and scroll if message count changed or initial load
        if (isInitial || msgs.length !== this.messages.length) {
          this.messages = msgs;
          this.shouldScroll = true;
        }
      },
      error: () => undefined
    });
  }

  startMessagePolling(roomId: number) {
    this.clearMessagePolling();
    // Poll messages every 2 seconds
    this.pollIntervalId = setInterval(() => {
      this.loadMessages(roomId);
    }, 2000);
  }

  clearMessagePolling() {
    if (this.pollIntervalId) {
      clearInterval(this.pollIntervalId);
      this.pollIntervalId = null;
    }
  }

  sendMessage() {
    if (!this.selectedRoomId || !this.newMessageContent.trim() || this.sending) return;

    const content = this.newMessageContent.trim();
    this.sending = true;

    this.api.sendChatMessage(this.selectedRoomId, content).subscribe({
      next: response => {
        this.newMessageContent = '';
        this.sending = false;
        
        // Append message and scroll immediately
        if (response.data) {
          this.messages = [...this.messages, response.data];
          this.shouldScroll = true;
          
          // Also update the last message in local chatRooms list to show in sidebar
          if (this.selectedRoom) {
            this.selectedRoom.lastMessage = response.data;
          }
        }
      },
      error: () => {
        this.sending = false;
        this.toast.show('Failed to send message.', 'error');
      }
    });
  }

  isMyMessage(msg: ChatMessageResponse): boolean {
    return msg.senderId === this.currentUserId;
  }

  getParticipantName(room: ChatRoomResponse): string {
    if (this.auth.hasRole('OWNER')) {
      return room.tenantName;
    }
    return room.ownerName;
  }

  formatTime(dateTimeStr: string): string {
    if (!dateTimeStr) return '';
    try {
      const date = new Date(dateTimeStr);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return '';
    }
  }

  formatDate(dateTimeStr: string): string {
    if (!dateTimeStr) return '';
    try {
      const date = new Date(dateTimeStr);
      return date.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' });
    } catch {
      return '';
    }
  }

  shouldShowDateSeparator(index: number): boolean {
    if (index === 0) return true;
    try {
      const current = new Date(this.messages[index].sentAt).toDateString();
      const prev = new Date(this.messages[index - 1].sentAt).toDateString();
      return current !== prev;
    } catch {
      return false;
    }
  }

  private scrollToBottom(): void {
    try {
      setTimeout(() => {
        if (this.scrollContainer && this.scrollContainer.nativeElement) {
          const el = this.scrollContainer.nativeElement;
          el.scrollTop = el.scrollHeight;
        }
      }, 50);
    } catch (err) {
      // ignore
    }
  }
}
