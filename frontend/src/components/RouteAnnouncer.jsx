import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

const pageTitles = {
  '/': 'Home',
  '/login': 'Login',
  '/register': 'Register',
  '/analyze': 'Analyze',
  '/explore': 'Explore',
  '/troubleshoot': 'Troubleshoot',
  '/collaborate': 'Collaborate',
  '/profile': 'Profile',
};

const RouteAnnouncer = () => {
  const location = useLocation();
  const [announcement, setAnnouncement] = useState('');

  useEffect(() => {
    const title = pageTitles[location.pathname] || 'FoodSecurityNet';
    document.title = `${title} | FoodSecurityNet`;
    setAnnouncement(`${title} page loaded`);
  }, [location.pathname]);

  return (
    <div className="sr-only" aria-live="polite" aria-atomic="true">
      {announcement}
    </div>
  );
};

export default RouteAnnouncer;
