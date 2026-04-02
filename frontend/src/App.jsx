import React, { useState, useEffect, lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ErrorBoundary from './components/ErrorBoundary';
import ProtectedRoute from './components/ProtectedRoute';
import RouteAnnouncer from './components/RouteAnnouncer';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import { authService } from './services/auth';

const Analyze = lazy(() => import('./pages/Analyze'));
const Explore = lazy(() => import('./pages/Explore'));
const Troubleshoot = lazy(() => import('./pages/Troubleshoot'));
const Collaborate = lazy(() => import('./pages/Collaborate'));
const Profile = lazy(() => import('./pages/Profile'));

const RouteFallback = () => (
  <div className="min-h-[60vh] flex items-center justify-center bg-gray-50" role="status" aria-live="polite">
    <div className="text-center">
      <div className="animate-spin rounded-full h-14 w-14 border-t-2 border-b-2 border-primary-600 mx-auto" aria-hidden="true"></div>
      <p className="mt-4 text-gray-600">Loading page...</p>
    </div>
  </div>
);

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is authenticated on mount
    const checkAuth = async () => {
      try {
        const currentUser = await authService.getCurrentUser();
        setUser(currentUser);
      } catch (error) {
        console.error('Auth check failed:', error);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
  };

  const handleLogout = async () => {
    try {
      await authService.logout();
      setUser(null);
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50" role="status" aria-live="polite">
        <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-primary-600" aria-hidden="true"></div>
        <span className="sr-only">Loading application</span>
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <div className="min-h-screen flex flex-col bg-gray-50">
        <a href="#main-content" className="skip-link">
          Skip to main content
        </a>
        <RouteAnnouncer />
        <Navbar user={user} onLogout={handleLogout} />

        <main id="main-content" tabIndex="-1" className="flex-grow">
          <Suspense fallback={<RouteFallback />}>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route
                path="/login"
                element={user ? <Navigate to="/analyze" /> : <Login onLogin={handleLogin} />}
              />
              <Route
                path="/register"
                element={user ? <Navigate to="/analyze" /> : <Register onLogin={handleLogin} />}
              />

              <Route
                path="/analyze"
                element={
                  <ProtectedRoute user={user}>
                    <Analyze user={user} />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/explore"
                element={
                  <ProtectedRoute user={user}>
                    <Explore user={user} />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/troubleshoot"
                element={
                  <ProtectedRoute user={user}>
                    <Troubleshoot user={user} />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/collaborate"
                element={
                  <ProtectedRoute user={user}>
                    <Collaborate user={user} />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/profile"
                element={
                  <ProtectedRoute user={user}>
                    <Profile user={user} onUserUpdate={setUser} />
                  </ProtectedRoute>
                }
              />

              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </Suspense>
        </main>

        <Footer />

        <ToastContainer
          position="top-right"
          autoClose={5000}
          hideProgressBar={false}
          newestOnTop
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
          theme="light"
        />
      </div>
    </ErrorBoundary>
  );
}

export default App;
