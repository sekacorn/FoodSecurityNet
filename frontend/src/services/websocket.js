import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_BASE_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.connectPromise = null;
    this.eventHandlers = {};
    this.sessionSubscriptions = [];
  }

  connect() {
    if (this.connected) {
      return Promise.resolve();
    }

    if (this.connectPromise) {
      return this.connectPromise;
    }

    this.connectPromise = new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
        reconnectDelay: 5000,
        debug: () => {},
        onConnect: () => {
          this.connected = true;
          this.emit('connected');
          resolve();
        },
        onStompError: (frame) => {
          const error = new Error(frame.headers.message || 'STOMP error');
          this.emit('error', error);
          reject(error);
        },
        onWebSocketError: (event) => {
          this.emit('error', event);
          reject(event);
        },
        onDisconnect: () => {
          this.connected = false;
          this.clearSessionSubscriptions();
          this.emit('disconnected');
          this.connectPromise = null;
        },
      });

      this.client.activate();
    });

    return this.connectPromise;
  }

  disconnect() {
    this.clearSessionSubscriptions();
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.connected = false;
    this.connectPromise = null;
  }

  clearSessionSubscriptions() {
    this.sessionSubscriptions.forEach((subscription) => subscription.unsubscribe());
    this.sessionSubscriptions = [];
  }

  on(event, handler) {
    if (!this.eventHandlers[event]) {
      this.eventHandlers[event] = [];
    }
    this.eventHandlers[event].push(handler);
  }

  off(event, handler) {
    if (!this.eventHandlers[event]) return;

    if (handler) {
      this.eventHandlers[event] = this.eventHandlers[event].filter((h) => h !== handler);
    } else {
      delete this.eventHandlers[event];
    }
  }

  emit(event, ...args) {
    if (this.eventHandlers[event]) {
      this.eventHandlers[event].forEach((handler) => {
        try {
          handler(...args);
        } catch (error) {
          console.error(`Error in event handler for ${event}:`, error);
        }
      });
    }
  }

  async subscribeToSession(sessionId) {
    await this.connect();
    this.clearSessionSubscriptions();

    const sessionSubscription = this.client.subscribe(`/topic/session/${sessionId}`, (frame) => {
      const message = JSON.parse(frame.body);

      switch (message.type) {
        case 'USER_JOINED':
          this.emit('user-joined', message);
          break;
        case 'USER_LEFT':
          this.emit('user-left', message);
          break;
        case 'USER_ACTION':
          if (message.actionType === 'CHAT_MESSAGE') {
            this.emit('message', {
              userId: message.userId,
              userName: message.data?.userName || 'Collaborator',
              content: message.data?.content || '',
              timestamp: message.data?.timestamp || Date.now(),
            });
          } else {
            this.emit('action', message);
          }
          break;
        case 'ANNOTATION':
          this.emit('annotation', message);
          break;
        default:
          this.emit('message-received', message);
      }
    });

    const cursorSubscription = this.client.subscribe(
      `/topic/session/${sessionId}/cursors`,
      (frame) => {
        this.emit('cursor-move', JSON.parse(frame.body));
      }
    );

    this.sessionSubscriptions.push(sessionSubscription, cursorSubscription);
  }

  async joinSession(sessionId, user) {
    await this.subscribeToSession(sessionId);

    this.client.publish({
      destination: `/app/session/${sessionId}/join`,
      body: JSON.stringify({
        userId: user.id,
        userName: user.name,
      }),
    });
  }

  leaveSession(sessionId, user) {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: `/app/session/${sessionId}/leave`,
      body: JSON.stringify({
        userId: user.id,
      }),
    });

    this.clearSessionSubscriptions();
  }

  sendMessage(sessionId, message) {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: `/app/session/${sessionId}/action`,
      body: JSON.stringify({
        userId: message.userId,
        actionType: 'CHAT_MESSAGE',
        data: {
          content: message.content,
          userName: message.userName,
          timestamp: message.timestamp,
        },
      }),
    });
  }

  sendCursorPosition(sessionId, position) {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: `/app/session/${sessionId}/cursor`,
      body: JSON.stringify(position),
    });
  }

  addAnnotation(sessionId, annotation) {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: `/app/session/${sessionId}/annotation`,
      body: JSON.stringify(annotation),
    });
  }

  isConnected() {
    return this.connected;
  }
}

const websocketService = new WebSocketService();

export { websocketService };
export default websocketService;
