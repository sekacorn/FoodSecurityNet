# FoodSecurityNet Frontend

React/Vite frontend for the current FoodSecurityNet application.

## Current Frontend Scope

- Authentication with profile and MFA flows
- Data upload and AI analysis results
- 3D exploration views
- Troubleshooting chat and resource monitoring
- Collaboration session browsing and joining
- Export support for PNG, SVG, STL, JSON, and CSV

## Stack

- React 18
- Vite 5
- React Router 6
- Tailwind CSS
- Axios
- Three.js with `@react-three/fiber` and `@react-three/drei`
- STOMP over SockJS for collaboration
- React Toastify

## Environment

Copy `.env.example` to `.env` and verify these values:

```env
VITE_API_URL=http://localhost:8080
VITE_WS_URL=http://localhost:8080
```

## Commands

```bash
npm install
npm run dev
npm run build
npm run preview
```

Default dev URL: [http://127.0.0.1:3000](http://127.0.0.1:3000)

## Notes

- The active frontend no longer uses MBTI-driven fields or UI customization
- Collaboration no longer uses `socket.io`; it uses STOMP/SockJS via [`websocket.js`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/services/websocket.js)
- Accessibility improvements include skip navigation, route announcements, and improved keyboard/screen-reader support

## Important Files

- [`src/App.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/App.jsx)
- [`src/components/ExportTool.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/components/ExportTool.jsx)
- [`src/components/RouteAnnouncer.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/components/RouteAnnouncer.jsx)
- [`src/pages/Analyze.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/pages/Analyze.jsx)
- [`src/pages/Explore.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/pages/Explore.jsx)
- [`src/pages/Troubleshoot.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/pages/Troubleshoot.jsx)
- [`src/pages/Profile.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/pages/Profile.jsx)

## License

MIT License. See [`../LICENSE`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/LICENSE).
