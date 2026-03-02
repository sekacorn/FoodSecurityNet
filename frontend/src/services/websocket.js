import { io } from 'socket.io-client';

const WS_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8000';

class WebSocketService {
  constructor() {
    this.socket = null;
    this.connected = false;
    this.eventHandlers = {};
  }

  connect() {
    if (this.socket && this.connected) {
      console.log('WebSocket already connected');
      return;
    }

    const token = localStorage.getItem('access_token');

    this.socket = io(WS_URL, {
      transports: ['websocket', 'polling'],
      auth: {
        token: token,
      },
      reconnection: true,
      reconnectionAttempts: 5,
      reconnectionDelay: 1000,
    });

    this.socket.on('connect', () => {
      console.log('WebSocket connected');
      this.connected = true;
      this.emit('connected');
    });

    this.socket.on('disconnect', (reason) => {
      console.log('WebSocket disconnected:', reason);
      this.connected = false;
      this.emit('disconnected', reason);
    });

    this.socket.on('error', (error) => {
      console.error('WebSocket error:', error);
      this.emit('error', error);
    });

    this.socket.on('connect_error', (error) => {
      console.error('WebSocket connection error:', error);
      this.emit('error', error);
    });

    // Register for all events
    this.socket.onAny((eventName, ...args) => {
      this.emit(eventName, ...args);
    });
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
      this.connected = false;
      this.eventHandlers = {};
    }
  }

  // Event handling
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

  // Send event to server
  send(event, data) {
    if (this.socket && this.connected) {
      this.socket.emit(event, data);
    } else {
      console.error('WebSocket not connected');
    }
  }

  // Collaboration methods
  joinSession(sessionId, user) {
    this.send('join-session', {
      sessionId,
      userId: user.id,
      userName: user.name,
    });
  }

  leaveSession(sessionId) {
    this.send('leave-session', { sessionId });
  }

  sendMessage(sessionId, message) {
    this.send('message', {
      sessionId,
      ...message,
    });
  }

  sendCursorPosition(sessionId, position) {
    this.send('cursor-move', {
      sessionId,
      ...position,
    });
  }

  // Data sync methods
  syncData(sessionId, data) {
    this.send('sync-data', {
      sessionId,
      data,
    });
  }

  requestDataSync(sessionId) {
    this.send('request-sync', { sessionId });
  }

  // Annotation methods
  addAnnotation(sessionId, annotation) {
    this.send('add-annotation', {
      sessionId,
      annotation,
    });
  }

  removeAnnotation(sessionId, annotationId) {
    this.send('remove-annotation', {
      sessionId,
      annotationId,
    });
  }

  // Utility methods
  isConnected() {
    return this.connected;
  }

  getSocket() {
    return this.socket;
  }
}

// Create singleton instance
const websocketService = new WebSocketService();

export { websocketService };
export default websocketService;
