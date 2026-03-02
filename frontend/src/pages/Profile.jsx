import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';
import api from '../services/api';

const Profile = ({ user, onUserUpdate }) => {
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: user.name || '',
    email: user.email || '',
    mbti: user.mbti || 'ISTJ',
  });
  const [mfaEnabled, setMfaEnabled] = useState(user.mfa_enabled || false);
  const [showMFASetup, setShowMFASetup] = useState(false);
  const [mfaSecret, setMfaSecret] = useState(null);
  const [mfaCode, setMfaCode] = useState('');
  const [loading, setLoading] = useState(false);

  const mbtiTypes = [
    'ISTJ', 'ISFJ', 'INFJ', 'INTJ',
    'ISTP', 'ISFP', 'INFP', 'INTP',
    'ESTP', 'ESFP', 'ENFP', 'ENTP',
    'ESTJ', 'ESFJ', 'ENFJ', 'ENTJ',
  ];

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await api.put('/users/profile', formData);
      onUserUpdate(response.data.user);
      setEditing(false);
      toast.success('Profile updated successfully!');
    } catch (error) {
      console.error('Profile update error:', error);
      toast.error('Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const handleEnableMFA = async () => {
    try {
      const response = await api.post('/auth/mfa/setup');
      setMfaSecret(response.data);
      setShowMFASetup(true);
    } catch (error) {
      console.error('MFA setup error:', error);
      toast.error('Failed to setup MFA');
    }
  };

  const handleVerifyMFA = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      await api.post('/auth/mfa/verify', {
        code: mfaCode,
        secret: mfaSecret.secret,
      });

      setMfaEnabled(true);
      setShowMFASetup(false);
      setMfaCode('');
      toast.success('MFA enabled successfully!');
      onUserUpdate({ ...user, mfa_enabled: true });
    } catch (error) {
      console.error('MFA verification error:', error);
      toast.error('Invalid MFA code');
    } finally {
      setLoading(false);
    }
  };

  const handleDisableMFA = async () => {
    if (!window.confirm('Are you sure you want to disable MFA?')) return;

    try {
      await api.post('/auth/mfa/disable');
      setMfaEnabled(false);
      toast.success('MFA disabled successfully!');
      onUserUpdate({ ...user, mfa_enabled: false });
    } catch (error) {
      console.error('MFA disable error:', error);
      toast.error('Failed to disable MFA');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Profile Settings</h1>
          <p className="text-gray-600">Manage your account settings and preferences</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Profile Picture & Info */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow p-6">
              <div className="text-center">
                <div className="w-24 h-24 bg-primary-600 rounded-full flex items-center justify-center text-white text-4xl font-bold mx-auto mb-4">
                  {user.name?.charAt(0).toUpperCase() || 'U'}
                </div>
                <h2 className="text-xl font-semibold text-gray-900">{user.name}</h2>
                <p className="text-gray-600">{user.email}</p>
                <div className="mt-4">
                  <span className="inline-block px-3 py-1 bg-primary-100 text-primary-800 rounded-full text-sm font-medium">
                    {user.mbti || 'ISTJ'}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Settings Forms */}
          <div className="lg:col-span-2 space-y-6">
            {/* Personal Information */}
            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-semibold text-gray-900">Personal Information</h3>
                {!editing && (
                  <button
                    onClick={() => setEditing(true)}
                    className="text-primary-600 hover:text-primary-700 font-medium"
                  >
                    Edit
                  </button>
                )}
              </div>

              {editing ? (
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Full Name
                    </label>
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleChange}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Email Address
                    </label>
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      MBTI Personality Type
                    </label>
                    <select
                      name="mbti"
                      value={formData.mbti}
                      onChange={handleChange}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    >
                      {mbtiTypes.map((type) => (
                        <option key={type} value={type}>
                          {type}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="flex space-x-3 pt-4">
                    <button
                      type="submit"
                      disabled={loading}
                      className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-400 transition-colors"
                    >
                      {loading ? 'Saving...' : 'Save Changes'}
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setEditing(false);
                        setFormData({
                          name: user.name || '',
                          email: user.email || '',
                          mbti: user.mbti || 'ISTJ',
                        });
                      }}
                      className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              ) : (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-500 mb-1">
                      Full Name
                    </label>
                    <p className="text-gray-900">{user.name}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-500 mb-1">
                      Email Address
                    </label>
                    <p className="text-gray-900">{user.email}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-500 mb-1">
                      MBTI Personality Type
                    </label>
                    <p className="text-gray-900">{user.mbti || 'ISTJ'}</p>
                  </div>
                </div>
              )}
            </div>

            {/* Security Settings */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-6">Security</h3>

              {/* MFA Section */}
              <div className="border-b border-gray-200 pb-6 mb-6">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h4 className="font-medium text-gray-900">Two-Factor Authentication</h4>
                    <p className="text-sm text-gray-600">
                      Add an extra layer of security to your account
                    </p>
                  </div>
                  <div
                    className={`px-3 py-1 rounded-full text-sm font-medium ${
                      mfaEnabled
                        ? 'bg-green-100 text-green-800'
                        : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {mfaEnabled ? 'Enabled' : 'Disabled'}
                  </div>
                </div>

                {!mfaEnabled ? (
                  <button
                    onClick={handleEnableMFA}
                    className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
                  >
                    Enable MFA
                  </button>
                ) : (
                  <button
                    onClick={handleDisableMFA}
                    className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                  >
                    Disable MFA
                  </button>
                )}
              </div>

              {/* Password Change */}
              <div>
                <h4 className="font-medium text-gray-900 mb-4">Change Password</h4>
                <button className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
                  Update Password
                </button>
              </div>
            </div>

            {/* Account Stats */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-6">Account Activity</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="text-center p-4 bg-gray-50 rounded-lg">
                  <p className="text-2xl font-bold text-primary-600">
                    {user.analyses_count || 0}
                  </p>
                  <p className="text-sm text-gray-600">Analyses Run</p>
                </div>
                <div className="text-center p-4 bg-gray-50 rounded-lg">
                  <p className="text-2xl font-bold text-primary-600">
                    {user.collaborations_count || 0}
                  </p>
                  <p className="text-sm text-gray-600">Collaborations</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* MFA Setup Modal */}
      {showMFASetup && mfaSecret && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h3 className="text-xl font-semibold text-gray-900 mb-4">Setup Two-Factor Authentication</h3>

            <div className="mb-6">
              <p className="text-sm text-gray-600 mb-4">
                Scan this QR code with your authenticator app:
              </p>
              <div className="flex justify-center mb-4">
                <img src={mfaSecret.qr_code} alt="MFA QR Code" className="w-48 h-48" />
              </div>
              <p className="text-xs text-gray-500 text-center">
                Or enter this code manually: <br />
                <code className="bg-gray-100 px-2 py-1 rounded">{mfaSecret.secret}</code>
              </p>
            </div>

            <form onSubmit={handleVerifyMFA}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Enter 6-digit code from your app
                </label>
                <input
                  type="text"
                  value={mfaCode}
                  onChange={(e) => setMfaCode(e.target.value)}
                  maxLength="6"
                  placeholder="000000"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg text-center text-2xl tracking-widest focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  required
                />
              </div>

              <div className="flex space-x-3">
                <button
                  type="button"
                  onClick={() => {
                    setShowMFASetup(false);
                    setMfaCode('');
                  }}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading || mfaCode.length !== 6}
                  className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-400 transition-colors"
                >
                  {loading ? 'Verifying...' : 'Verify'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

Profile.propTypes = {
  user: PropTypes.object.isRequired,
  onUserUpdate: PropTypes.func.isRequired,
};

export default Profile;
