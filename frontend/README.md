# FoodSecurityNet Frontend

React-based frontend application for the FoodSecurityNet agricultural security platform.

## Features

- **Data Analysis**: Upload and analyze agricultural data with AI-powered insights
- **3D Visualization**: Interactive 3D models using Three.js for farm layouts and crop yields
- **LLM Assistant**: Natural language chat interface for agricultural recommendations
- **Real-time Collaboration**: WebSocket-based collaboration with live cursor tracking
- **MBTI Personalization**: Customized UI and recommendations based on user personality type
- **Multi-factor Authentication**: Enhanced security with MFA support
- **Responsive Design**: Mobile-first design with Tailwind CSS
- **SSO Integration**: Google and GitHub OAuth support

## Tech Stack

- **React 18**: UI framework
- **Vite**: Build tool and dev server
- **React Router v6**: Client-side routing
- **Three.js & React Three Fiber**: 3D graphics
- **Axios**: HTTP client
- **Socket.IO**: Real-time WebSocket communication
- **Tailwind CSS**: Utility-first CSS framework
- **Plotly.js**: Data visualization
- **React Toastify**: Notifications

## Prerequisites

- Node.js 18+ and npm
- Backend API running (see backend README)

## Installation

1. Install dependencies:
```bash
npm install
```

2. Copy environment variables:
```bash
cp .env.example .env
```

3. Update `.env` with your configuration:
```env
VITE_API_URL=http://localhost:8000
VITE_WS_URL=ws://localhost:8000
```

## Development

Start the development server:
```bash
npm run dev
```

The application will be available at `http://localhost:3000`

## Build

Create a production build:
```bash
npm run build
```

Preview the production build:
```bash
npm run preview
```

## Docker

Build the Docker image:
```bash
docker build -t foodsecuritynet-frontend .
```

Run the container:
```bash
docker run -p 80:80 foodsecuritynet-frontend
```

## Project Structure

```
frontend/
├── public/              # Static assets
├── src/
│   ├── components/      # React components
│   │   ├── AgriDetails.jsx
│   │   ├── AgriViewer.jsx
│   │   ├── AnnotationTool.jsx
│   │   ├── CollabPanel.jsx
│   │   ├── DataUpload.jsx
│   │   ├── ExportTool.jsx
│   │   ├── Footer.jsx
│   │   ├── LLMChat.jsx
│   │   ├── Navbar.jsx
│   │   ├── ProtectedRoute.jsx
│   │   └── ResourceMonitor.jsx
│   ├── pages/           # Page components
│   │   ├── Analyze.jsx
│   │   ├── Collaborate.jsx
│   │   ├── Explore.jsx
│   │   ├── Home.jsx
│   │   ├── Login.jsx
│   │   ├── Profile.jsx
│   │   ├── Register.jsx
│   │   └── Troubleshoot.jsx
│   ├── services/        # API and service clients
│   │   ├── api.js
│   │   ├── auth.js
│   │   └── websocket.js
│   ├── utils/           # Utility functions
│   │   ├── mbtiStyles.js
│   │   └── validators.js
│   ├── App.jsx          # Main app component
│   ├── main.jsx         # Entry point
│   └── index.css        # Global styles
├── index.html
├── vite.config.js
├── tailwind.config.js
├── package.json
├── Dockerfile
└── nginx.conf
```

## Key Components

### Data Upload
- Drag-and-drop file upload
- CSV/JSON support
- File validation and progress tracking

### 3D Viewer
- Interactive 3D farm layouts
- Crop yield visualizations
- Orbit controls (zoom, pan, rotate)

### Collaboration
- Real-time chat
- Live cursor tracking
- Session management

### Authentication
- Email/password login
- SSO (Google, GitHub)
- Multi-factor authentication
- Protected routes

## MBTI Personalization

The application customizes UI and recommendations based on 16 MBTI personality types:

- **Color schemes**: Each type has unique colors
- **Presentation style**: Content adapted to personality preferences
- **Detail level**: Information density adjusted per type
- **Action labels**: Personalized call-to-action text

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_URL` | Backend API URL | `http://localhost:8000` |
| `VITE_WS_URL` | WebSocket URL | `ws://localhost:8000` |
| `VITE_APP_NAME` | Application name | `FoodSecurityNet` |
| `VITE_ENABLE_3D_VIEWER` | Enable 3D features | `true` |
| `VITE_ENABLE_LLM_CHAT` | Enable LLM chat | `true` |
| `VITE_ENABLE_COLLABORATION` | Enable collaboration | `true` |

## API Endpoints

The frontend communicates with these backend endpoints:

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/auth/me` - Get current user
- `POST /api/analyze` - Analyze agricultural data
- `GET /api/visualizations/:id` - Get visualization data
- `POST /api/llm/chat` - LLM chat interface
- `GET /api/system/resources` - System resource monitoring
- `GET /api/collaboration/sessions` - Get collaboration sessions

## WebSocket Events

- `connect` - Connection established
- `join-session` - Join collaboration session
- `leave-session` - Leave session
- `message` - Chat message
- `cursor-move` - Cursor position update
- `sync-data` - Data synchronization

## Security

- Content Security Policy headers
- XSS protection
- CSRF token support
- Secure cookie handling
- Input sanitization
- JWT token refresh

## Accessibility

- ARIA labels and roles
- Keyboard navigation
- Screen reader support
- Focus indicators
- Skip to main content link

## Performance

- Code splitting
- Lazy loading
- Image optimization
- Gzip compression
- Browser caching
- Service worker ready

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

MIT License - See LICENSE file for details
