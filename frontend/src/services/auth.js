import api from './api';

const normalizeUser = (user) => {
  if (!user) return null;

  return {
    id: user.id,
    username: user.username,
    name: user.fullName || user.username || 'User',
    email: user.email,
    role: user.role,
    mfa_enabled: Boolean(user.mfaEnabled),
    email_verified: Boolean(user.emailVerified),
    is_active: Boolean(user.isActive),
    last_login: user.lastLogin,
    created_at: user.createdAt,
    updated_at: user.updatedAt,
  };
};

const normalizeLoginResponse = (payload) => {
  if (!payload) return null;

  return {
    access_token: payload.accessToken,
    refresh_token: payload.refreshToken,
    expires_in: payload.expiresIn,
    requires_mfa: Boolean(payload.mfaRequired),
    user: payload.mfaRequired
      ? null
      : normalizeUser({
          id: payload.userId,
          username: payload.username,
          email: payload.email,
          fullName: payload.fullName,
          role: payload.role,
          mfaEnabled: payload.mfaEnabled,
          emailVerified: payload.emailVerified,
          isActive: true,
        }),
  };
};

const createUsername = (name, email) => {
  const preferred = (name || email.split('@')[0] || 'user')
    .toLowerCase()
    .replace(/[^a-z0-9_-]/g, '');

  return preferred.slice(0, 50) || `user${Date.now()}`;
};

class AuthService {
  async login(credentials) {
    const response = await api.post('/auth/login', {
      usernameOrEmail: credentials.email,
      password: credentials.password,
      mfaCode: credentials.mfa_code || undefined,
    });

    const normalized = normalizeLoginResponse(response.data);

    if (normalized?.access_token) {
      localStorage.setItem('access_token', normalized.access_token);
      if (normalized.refresh_token) {
        localStorage.setItem('refresh_token', normalized.refresh_token);
      }
    }

    return normalized;
  }

  async register(userData) {
    const response = await api.post('/auth/register', {
      username: createUsername(userData.name, userData.email),
      email: userData.email,
      password: userData.password,
      fullName: userData.name,
      role: 'USER',
    });

    return {
      user: normalizeUser(response.data?.data),
    };
  }

  async logout() {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
    }
  }

  async getCurrentUser() {
    const token = localStorage.getItem('access_token');
    if (!token) {
      return null;
    }

    try {
      const response = await api.get('/auth/me');
      return normalizeUser(response.data?.data);
    } catch (error) {
      if (error.response?.status === 401) {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
      }
      return null;
    }
  }

  async updateCurrentUser(profile) {
    const response = await api.put('/auth/me', {
      fullName: profile.name,
      email: profile.email,
    });

    return normalizeUser(response.data?.data);
  }

  async refreshToken() {
    const refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await api.post('/auth/refresh', { refreshToken });
    const normalized = normalizeLoginResponse(response.data);

    if (normalized?.access_token) {
      localStorage.setItem('access_token', normalized.access_token);
    }

    return normalized;
  }

  async ssoLogin(provider) {
    window.location.href = `${import.meta.env.VITE_API_URL}/oauth2/authorization/${provider}`;
  }

  async setupMFA() {
    const response = await api.post('/auth/mfa/setup');
    return response.data?.data;
  }

  async verifyMFA(code, secret) {
    const response = await api.post('/auth/mfa/verify', { code, secret });
    return response.data?.data;
  }

  async disableMFA(code) {
    const response = await api.post('/auth/mfa/disable', { code });
    return response.data?.data;
  }

  isAuthenticated() {
    return !!localStorage.getItem('access_token');
  }

  getToken() {
    return localStorage.getItem('access_token');
  }
}

const authService = new AuthService();

export { authService, normalizeUser };
export default authService;
