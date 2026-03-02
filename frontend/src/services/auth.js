import api from './api';

class AuthService {
  // Login
  async login(credentials) {
    try {
      const response = await api.post('/auth/login', credentials);

      if (response.data.access_token) {
        localStorage.setItem('access_token', response.data.access_token);
        if (response.data.refresh_token) {
          localStorage.setItem('refresh_token', response.data.refresh_token);
        }
      }

      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Register
  async register(userData) {
    try {
      const response = await api.post('/auth/register', userData);

      if (response.data.access_token) {
        localStorage.setItem('access_token', response.data.access_token);
        if (response.data.refresh_token) {
          localStorage.setItem('refresh_token', response.data.refresh_token);
        }
      }

      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Logout
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

  // Get current user
  async getCurrentUser() {
    try {
      const token = localStorage.getItem('access_token');
      if (!token) {
        return null;
      }

      const response = await api.get('/auth/me');
      return response.data.user || response.data;
    } catch (error) {
      console.error('Get current user error:', error);
      // If unauthorized, clear tokens
      if (error.response?.status === 401) {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
      }
      return null;
    }
  }

  // Refresh token
  async refreshToken() {
    try {
      const refreshToken = localStorage.getItem('refresh_token');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await api.post('/auth/refresh', {
        refresh_token: refreshToken,
      });

      if (response.data.access_token) {
        localStorage.setItem('access_token', response.data.access_token);
      }

      return response.data;
    } catch (error) {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      throw error;
    }
  }

  // SSO Login
  async ssoLogin(provider) {
    try {
      // Redirect to SSO provider
      window.location.href = `${import.meta.env.VITE_API_URL}/api/auth/sso/${provider}`;
    } catch (error) {
      console.error('SSO login error:', error);
      throw error;
    }
  }

  // Handle SSO callback
  async handleSSOCallback(code, provider) {
    try {
      const response = await api.post('/auth/sso/callback', {
        code,
        provider,
      });

      if (response.data.access_token) {
        localStorage.setItem('access_token', response.data.access_token);
        if (response.data.refresh_token) {
          localStorage.setItem('refresh_token', response.data.refresh_token);
        }
      }

      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // MFA methods
  async setupMFA() {
    try {
      const response = await api.post('/auth/mfa/setup');
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async verifyMFA(code, secret) {
    try {
      const response = await api.post('/auth/mfa/verify', {
        code,
        secret,
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async disableMFA() {
    try {
      const response = await api.post('/auth/mfa/disable');
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Password reset
  async requestPasswordReset(email) {
    try {
      const response = await api.post('/auth/password-reset/request', {
        email,
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  async resetPassword(token, newPassword) {
    try {
      const response = await api.post('/auth/password-reset/confirm', {
        token,
        password: newPassword,
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Check authentication status
  isAuthenticated() {
    return !!localStorage.getItem('access_token');
  }

  // Get token
  getToken() {
    return localStorage.getItem('access_token');
  }
}

// Create singleton instance
const authService = new AuthService();

export { authService };
export default authService;
