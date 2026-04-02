import React, { useState, useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';
import api from '../services/api';
import { websocketService } from '../services/websocket';

const CollabPanel = ({ sessionId, user }) => {
  const [users, setUsers] = useState([]);
  const [userNames, setUserNames] = useState({});
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [remoteCursors, setRemoteCursors] = useState({});
  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (!sessionId) return;

    const bootstrapSession = async () => {
      try {
        const [usersResponse, historyResponse] = await Promise.all([
          api.get(`/collaboration/sessions/${sessionId}/users`),
          api.get(`/collaboration/sessions/${sessionId}/history`),
        ]);

        const activeUsers = usersResponse.data?.users || [];
        setUsers(activeUsers);
        setUserNames((prev) => ({
          ...prev,
          [user.id]: user.name,
        }));

        const historicalMessages = (historyResponse.data?.history || [])
          .filter((entry) => entry.actionType === 'CHAT_MESSAGE')
          .map((entry) => ({
            userId: entry.userId,
            userName: entry.actionData?.userName || entry.userId,
            content: entry.actionData?.content || '',
            timestamp: entry.actionData?.timestamp || entry.createdAt,
          }));

        setMessages(historicalMessages);

        await websocketService.joinSession(sessionId, user);
      } catch (error) {
        console.error('Failed to bootstrap collaboration session:', error);
        toast.error('Failed to connect to collaboration session');
      }
    };

    const handleConnected = () => {
      setIsConnected(true);
      toast.success('Connected to collaboration session');
    };

    const handleDisconnected = () => {
      setIsConnected(false);
      toast.info('Disconnected from collaboration session');
    };

    const handleUserJoined = (data) => {
      setUsers((prev) => Array.from(new Set([...prev, data.userId])));
      setUserNames((prev) => ({
        ...prev,
        [data.userId]: data.userName || data.userId,
      }));
      toast.info(`${data.userName || data.userId} joined the session`);
    };

    const handleUserLeft = (data) => {
      setUsers((prev) => prev.filter((id) => id !== data.userId));
      toast.info(`${data.userId} left the session`);
    };

    const handleMessage = (message) => {
      setMessages((prev) => [...prev, message]);
    };

    const handleCursorMove = (data) => {
      setRemoteCursors((prev) => ({
        ...prev,
        [data.userId]: { x: data.x, y: data.y, name: data.userName },
      }));
      setUserNames((prev) => ({
        ...prev,
        [data.userId]: data.userName || data.userId,
      }));
    };

    const handleError = (error) => {
      console.error('WebSocket error:', error);
      toast.error('Collaboration error occurred');
    };

    websocketService.on('connected', handleConnected);
    websocketService.on('disconnected', handleDisconnected);
    websocketService.on('user-joined', handleUserJoined);
    websocketService.on('user-left', handleUserLeft);
    websocketService.on('message', handleMessage);
    websocketService.on('cursor-move', handleCursorMove);
    websocketService.on('error', handleError);

    bootstrapSession();

    return () => {
      websocketService.off('connected', handleConnected);
      websocketService.off('disconnected', handleDisconnected);
      websocketService.off('user-joined', handleUserJoined);
      websocketService.off('user-left', handleUserLeft);
      websocketService.off('message', handleMessage);
      websocketService.off('cursor-move', handleCursorMove);
      websocketService.off('error', handleError);
      websocketService.leaveSession(sessionId, user);
      websocketService.disconnect();
    };
  }, [sessionId, user]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = (e) => {
    e.preventDefault();
    if (!inputMessage.trim() || !isConnected) return;

    const message = {
      userId: user.id,
      userName: user.name,
      content: inputMessage.trim(),
      timestamp: new Date(),
    };

    websocketService.sendMessage(sessionId, message);
    setInputMessage('');
  };

  const handleMouseMove = (e) => {
    const x = e.clientX;
    const y = e.clientY;

    if (isConnected) {
      websocketService.sendCursorPosition(sessionId, {
        userId: user.id,
        userName: user.name,
        x,
        y,
      });
    }
  };

  const getRandomColor = (userId = '0') => {
    const colors = ['#ef4444', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6', '#ec4899'];
    const index = String(userId).charCodeAt(0) % colors.length;
    return colors[index];
  };

  return (
    <div className="bg-white rounded-lg shadow-lg h-full flex flex-col">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-700 text-white px-6 py-4 rounded-t-lg">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="font-semibold text-lg">Collaboration Panel</h3>
            <p className="text-xs text-primary-100">Session: {sessionId}</p>
          </div>
          <div className="flex items-center space-x-2">
            <div
              className={`w-3 h-3 rounded-full ${
                isConnected ? 'bg-green-400 animate-pulse' : 'bg-red-400'
              }`}
            ></div>
            <span className="text-sm">{isConnected ? 'Connected' : 'Disconnected'}</span>
          </div>
        </div>
      </div>

      <div className="flex-1 flex overflow-hidden">
        {/* Sidebar - Active Users */}
        <div className="w-64 border-r border-gray-200 p-4 overflow-y-auto">
          <h4 className="font-semibold text-gray-900 mb-3">
            Active Users ({users.length})
          </h4>
          <div className="space-y-2">
            {users.map((userId) => (
              <div
                key={userId}
                className="flex items-center space-x-3 p-2 rounded-lg hover:bg-gray-50"
              >
                <div
                  className="w-8 h-8 rounded-full flex items-center justify-center text-white font-semibold text-sm"
                  style={{ backgroundColor: getRandomColor(userId) }}
                >
                  {(userNames[userId] || userId).charAt(0).toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-medium text-gray-900 truncate">{userNames[userId] || userId}</p>
                  <p className="text-xs text-gray-500">
                    {userId === user.id ? '(You)' : 'Collaborator'}
                  </p>
                </div>
              </div>
            ))}
          </div>

          {users.length === 0 && (
            <p className="text-sm text-gray-500 text-center py-4">
              No active users
            </p>
          )}
        </div>

        {/* Main - Chat */}
        <div className="flex-1 flex flex-col">
          {/* Messages */}
          <div
            className="flex-1 overflow-y-auto p-4 space-y-3"
            onMouseMove={handleMouseMove}
            style={{ position: 'relative' }}
          >
            {messages.map((msg, index) => (
              <div
                key={index}
                className={`flex ${
                  msg.userId === user.id ? 'justify-end' : 'justify-start'
                }`}
              >
                <div
                  className={`max-w-[70%] rounded-lg px-4 py-2 ${
                    msg.userId === user.id
                      ? 'bg-primary-600 text-white'
                      : 'bg-gray-100 text-gray-900'
                  }`}
                >
                  {msg.userId !== user.id && (
                    <p className="text-xs font-semibold mb-1 opacity-70">
                      {msg.userName}
                    </p>
                  )}
                  <p className="text-sm">{msg.content}</p>
                  <p
                    className={`text-xs mt-1 ${
                      msg.userId === user.id ? 'text-primary-100' : 'text-gray-500'
                    }`}
                  >
                    {new Date(msg.timestamp).toLocaleTimeString('en-US', {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </p>
                </div>
              </div>
            ))}

            {/* Remote Cursors */}
            {Object.entries(remoteCursors).map(([userId, cursorData]) => (
              <div
                key={userId}
                className="fixed pointer-events-none z-50"
                style={{
                  left: cursorData.x,
                  top: cursorData.y,
                  transition: 'all 0.1s ease-out',
                }}
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 20 20"
                  fill={getRandomColor(userId)}
                >
                  <path d="M0 0L20 8L8 10L6 20L0 0Z" />
                </svg>
                <span
                  className="ml-4 text-xs font-semibold text-white px-2 py-1 rounded"
                  style={{ backgroundColor: getRandomColor(userId) }}
                >
                  {cursorData.name}
                </span>
              </div>
            ))}

            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <form onSubmit={handleSendMessage} className="p-4 border-t border-gray-200">
            <div className="flex space-x-2">
              <input
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                placeholder={
                  isConnected ? 'Type a message...' : 'Connecting...'
                }
                disabled={!isConnected}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:bg-gray-100"
                aria-label="Chat message"
              />
              <button
                type="submit"
                disabled={!isConnected || !inputMessage.trim()}
                className="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                aria-label="Send message"
              >
                Send
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

CollabPanel.propTypes = {
  sessionId: PropTypes.string.isRequired,
  user: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
};

export default CollabPanel;
