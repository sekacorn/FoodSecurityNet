import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';
import CollabPanel from '../components/CollabPanel';
import api from '../services/api';

const normalizeSession = (session) => ({
  id: session.sessionId,
  name: session.sessionName,
  creatorId: session.creatorId,
  status: session.status,
  createdAt: session.createdAt,
});

const Collaborate = ({ user }) => {
  const [sessions, setSessions] = useState([]);
  const [currentSession, setCurrentSession] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newSessionName, setNewSessionName] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSessions();
  }, [user.id]);

  const loadSessions = async () => {
    setLoading(true);
    try {
      const response = await api.get('/collaboration/sessions', {
        params: { creatorId: user.id },
      });
      setSessions((response.data.sessions || []).map(normalizeSession));
    } catch (error) {
      console.error('Failed to load sessions:', error);
      toast.error('Failed to load collaboration sessions');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSession = async (e) => {
    e.preventDefault();
    if (!newSessionName.trim()) return;

    try {
      const response = await api.post('/collaboration/sessions/create', {
        sessionName: newSessionName,
        creatorId: user.id,
      });

      const newSession = normalizeSession(response.data.session);
      setSessions([...sessions, newSession]);
      setCurrentSession(newSession);
      setShowCreateModal(false);
      setNewSessionName('');
      toast.success('Session created successfully!');
    } catch (error) {
      console.error('Failed to create session:', error);
      toast.error('Failed to create session');
    }
  };

  const handleJoinSession = (session) => {
    setCurrentSession(session);
    toast.success(`Joined session: ${session.name}`);
  };

  const handleLeaveSession = () => {
    setCurrentSession(null);
    toast.info('Left collaboration session');
  };

  if (currentSession) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="mb-6 flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{currentSession.name}</h1>
              <p className="text-gray-600">Real-time collaboration session</p>
            </div>
            <button
              onClick={handleLeaveSession}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
            >
              Leave Session
            </button>
          </div>

          {/* Collaboration Panel */}
          <div style={{ height: 'calc(100vh - 250px)' }}>
            <CollabPanel sessionId={currentSession.id} user={user} />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Collaboration</h1>
            <p className="text-gray-600">
              Work together in real-time with other farmers and experts
            </p>
          </div>
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium"
          >
            + Create Session
          </button>
        </div>

        {loading ? (
          <div className="bg-white rounded-lg shadow p-12">
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-primary-600 mb-4"></div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Loading Sessions...</h3>
            </div>
          </div>
        ) : sessions.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {sessions.map((session) => (
              <div
                key={session.id}
                className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow p-6"
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1">
                    <h3 className="text-xl font-semibold text-gray-900 mb-1">
                      {session.name}
                    </h3>
                    <p className="text-sm text-gray-600">
                      Created by {session.creatorId || 'Unknown'}
                    </p>
                  </div>
                  <div className="flex items-center space-x-1">
                    <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                    <span className="text-xs text-gray-600">Active</span>
                  </div>
                </div>

                <div className="mb-4">
                  <div className="flex items-center space-x-2 text-sm text-gray-600">
                    <svg
                      className="w-5 h-5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
                      />
                    </svg>
                    <span>Live session</span>
                  </div>
                </div>

                <button
                  onClick={() => handleJoinSession(session)}
                  className="w-full px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium"
                >
                  Join Session
                </button>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow p-12">
            <div className="text-center">
              <svg
                className="w-24 h-24 mx-auto mb-6 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
                />
              </svg>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">No Active Sessions</h3>
              <p className="text-gray-600 mb-6">
                Create a new session to start collaborating with others
              </p>
              <button
                onClick={() => setShowCreateModal(true)}
                className="px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium"
              >
                Create Your First Session
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Create Session Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h3 className="text-xl font-semibold text-gray-900 mb-4">
              Create Collaboration Session
            </h3>
            <form onSubmit={handleCreateSession}>
              <div className="mb-4">
                <label htmlFor="session-name" className="block text-sm font-medium text-gray-700 mb-2">
                  Session Name
                </label>
                <input
                  id="session-name"
                  type="text"
                  value={newSessionName}
                  onChange={(e) => setNewSessionName(e.target.value)}
                  placeholder="e.g., Wheat Farming Discussion"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  required
                />
              </div>
              <div className="flex space-x-3">
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateModal(false);
                    setNewSessionName('');
                  }}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
                >
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

Collaborate.propTypes = {
  user: PropTypes.object.isRequired,
};

export default Collaborate;
