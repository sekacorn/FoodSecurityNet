const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const jwt = require('jsonwebtoken');

const app = express();
const PORT = 8080;
const JWT_SECRET = 'demo-secret-key';

app.use(cors());
app.use(bodyParser.json());

let nextUserId = 2;
let users = [
  {
    id: 1,
    username: 'demo',
    email: 'demo@foodsecuritynet.org',
    password: 'Demo123!',
    fullName: 'Demo User',
    role: 'USER',
    mfaEnabled: false,
    emailVerified: true,
    isActive: true,
    lastLogin: null,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

let sessions = [
  {
    id: 1,
    sessionId: 'session-demo-1',
    sessionName: 'Demo Planning Session',
    creatorId: '1',
    status: 'ACTIVE',
    createdAt: new Date().toISOString(),
  },
];

const sessionUsers = {
  'session-demo-1': ['1', 'advisor-1'],
};

const sessionHistory = {
  'session-demo-1': [
    {
      id: 1,
      sessionId: 'session-demo-1',
      userId: 'advisor-1',
      actionType: 'CHAT_MESSAGE',
      actionData: {
        userName: 'Field Advisor',
        content: 'Welcome to the mock collaboration room.',
        timestamp: new Date().toISOString(),
      },
      createdAt: new Date().toISOString(),
    },
  ],
};

const visualizations = [
  {
    id: 101,
    name: 'Demo Soil Moisture Map',
    type: 'heatmap',
    data: [
      { x: -4, y: 0, z: -2, width: 1.5, height: 2.6, depth: 1.5 },
      { x: -2, y: 0, z: -2, width: 1.5, height: 3.1, depth: 1.5 },
      { x: 0, y: 0, z: -2, width: 1.5, height: 1.8, depth: 1.5 },
      { x: 2, y: 0, z: -2, width: 1.5, height: 2.4, depth: 1.5 },
      { x: 4, y: 0, z: -2, width: 1.5, height: 3.4, depth: 1.5 },
      { x: -4, y: 0, z: 0, width: 1.5, height: 2.2, depth: 1.5 },
      { x: -2, y: 0, z: 0, width: 1.5, height: 2.9, depth: 1.5 },
      { x: 0, y: 0, z: 0, width: 1.5, height: 3.7, depth: 1.5 },
      { x: 2, y: 0, z: 0, width: 1.5, height: 2.1, depth: 1.5 },
      { x: 4, y: 0, z: 0, width: 1.5, height: 1.9, depth: 1.5 },
    ],
  },
];

const buildLoginPayload = (user) => ({
  accessToken: jwt.sign(
    {
      id: user.id,
      username: user.username,
      email: user.email,
      role: user.role,
    },
    JWT_SECRET,
    { expiresIn: '24h' }
  ),
  refreshToken: `refresh-${user.id}`,
  expiresIn: 86400,
  tokenType: 'Bearer',
  userId: user.id,
  username: user.username,
  email: user.email,
  fullName: user.fullName,
  role: user.role,
  mfaEnabled: user.mfaEnabled,
  mfaRequired: false,
  emailVerified: user.emailVerified,
});

const buildUserDto = (user) => ({
  id: user.id,
  username: user.username,
  email: user.email,
  fullName: user.fullName,
  role: user.role,
  mfaEnabled: user.mfaEnabled,
  emailVerified: user.emailVerified,
  isActive: user.isActive,
  lastLogin: user.lastLogin,
  createdAt: user.createdAt,
  updatedAt: user.updatedAt,
});

const authenticateToken = (req, res, next) => {
  const authHeader = req.headers.authorization;
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ message: 'Access token required' });
  }

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ message: 'Invalid or expired token' });
    }
    req.user = user;
    next();
  });
};

app.post('/api/auth/login', (req, res) => {
  const { usernameOrEmail, password } = req.body;
  const user = users.find(
    (candidate) =>
      (candidate.username === usernameOrEmail || candidate.email === usernameOrEmail) &&
      candidate.password === password
  );

  if (!user) {
    return res.status(401).json({ message: 'Invalid username or password' });
  }

  user.lastLogin = new Date().toISOString();
  user.updatedAt = new Date().toISOString();
  res.json(buildLoginPayload(user));
});

app.post('/api/auth/register', (req, res) => {
  const { username, email, password, fullName } = req.body;

  if (users.some((user) => user.username === username || user.email === email)) {
    return res.status(400).json({ message: 'Username or email already exists' });
  }

  const user = {
    id: nextUserId++,
    username,
    email,
    password,
    fullName,
    role: 'USER',
    mfaEnabled: false,
    emailVerified: true,
    isActive: true,
    lastLogin: null,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  users.push(user);
  res.status(201).json({
    success: true,
    message: 'User registered successfully',
    data: buildUserDto(user),
  });
});

app.post('/api/auth/refresh', (req, res) => {
  const demoUser = users[0];
  res.json(buildLoginPayload(demoUser));
});

app.post('/api/auth/logout', (_req, res) => {
  res.json({ success: true, message: 'Logged out successfully' });
});

app.get('/api/auth/me', authenticateToken, (req, res) => {
  const user = users.find((candidate) => candidate.id === req.user.id);
  if (!user) {
    return res.status(404).json({ message: 'User not found' });
  }

  res.json({
    success: true,
    message: 'User retrieved successfully',
    data: buildUserDto(user),
  });
});

app.put('/api/auth/me', authenticateToken, (req, res) => {
  const user = users.find((candidate) => candidate.id === req.user.id);
  if (!user) {
    return res.status(404).json({ message: 'User not found' });
  }

  user.fullName = req.body.fullName || user.fullName;
  user.email = req.body.email || user.email;
  user.updatedAt = new Date().toISOString();

  res.json({
    success: true,
    message: 'User updated successfully',
    data: buildUserDto(user),
  });
});

app.post('/api/auth/mfa/setup', authenticateToken, (_req, res) => {
  res.json({
    success: true,
    message: 'MFA setup initiated',
    data: {
      secret: 'DEMO-MFA-SECRET',
      qr_code:
        'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=otpauth://totp/FoodSecurityNet:demo?secret=DEMO-MFA-SECRET',
    },
  });
});

app.post('/api/auth/mfa/verify', authenticateToken, (req, res) => {
  if (!req.body.code || String(req.body.code).length !== 6) {
    return res.status(400).json({ message: 'Invalid MFA code' });
  }

  const user = users.find((candidate) => candidate.id === req.user.id);
  user.mfaEnabled = true;
  user.updatedAt = new Date().toISOString();

  res.json({
    success: true,
    message: 'MFA enabled successfully',
    data: { mfaEnabled: true },
  });
});

app.post('/api/auth/mfa/disable', authenticateToken, (req, res) => {
  if (!req.body.code || String(req.body.code).length !== 6) {
    return res.status(400).json({ message: 'Invalid MFA code' });
  }

  const user = users.find((candidate) => candidate.id === req.user.id);
  user.mfaEnabled = false;
  user.updatedAt = new Date().toISOString();

  res.json({
    success: true,
    message: 'MFA disabled successfully',
    data: null,
  });
});

app.post('/api/llm/query', authenticateToken, (req, res) => {
  const { query = '' } = req.body;
  const lower = query.toLowerCase();

  let response = 'Mock assistant response: focus on practical, data-backed next steps.';
  if (lower.includes('soil')) {
    response = 'Mock analysis: soil pH and nutrient balance look stable, but phosphorus supplementation would improve yield consistency.';
  } else if (lower.includes('water') || lower.includes('irrigation')) {
    response = 'Mock analysis: switch to shorter, more frequent irrigation cycles to reduce runoff and improve moisture retention.';
  } else if (lower.includes('crop') || lower.includes('plant')) {
    response = 'Mock analysis: wheat and maize remain the strongest candidates for this sample dataset based on moisture and nutrient patterns.';
  }

  res.json({
    status: 'success',
    response,
  });
});

app.post('/api/llm/analyze', authenticateToken, (req, res) => {
  const analysisType = req.body.analysisType || 'dataset';

  res.json({
    status: 'success',
    analysis: {
      analysisType,
      insights:
        `Mock ${analysisType} analysis complete. The sample dataset suggests stable soil health, moderate water efficiency, and a near-term opportunity to improve fertilization timing.`,
      timestamp: new Date().toISOString(),
    },
  });
});

app.get('/api/visualizations/list', authenticateToken, (_req, res) => {
  res.json({
    status: 'success',
    data: visualizations,
    totalCount: visualizations.length,
  });
});

app.get('/api/visualizations/:id', authenticateToken, (req, res) => {
  const visualization = visualizations.find((item) => String(item.id) === String(req.params.id));
  if (!visualization) {
    return res.status(404).json({ error: 'Visualization not found' });
  }

  res.json({
    status: 'success',
    data: visualization,
  });
});

app.get('/api/collaboration/sessions', authenticateToken, (req, res) => {
  const filteredSessions = sessions.filter((session) => !req.query.creatorId || session.creatorId === String(req.query.creatorId));
  res.json({
    status: 'success',
    sessions: filteredSessions,
    count: filteredSessions.length,
  });
});

app.post('/api/collaboration/sessions/create', authenticateToken, (req, res) => {
  const session = {
    id: sessions.length + 1,
    sessionId: `session-demo-${sessions.length + 1}`,
    sessionName: req.body.sessionName || 'Untitled Session',
    creatorId: String(req.body.creatorId || req.user.id),
    status: 'ACTIVE',
    createdAt: new Date().toISOString(),
  };

  sessions.push(session);
  sessionUsers[session.sessionId] = [String(req.user.id)];
  sessionHistory[session.sessionId] = [];

  res.json({
    status: 'success',
    session,
  });
});

app.get('/api/collaboration/sessions/:sessionId/users', authenticateToken, (req, res) => {
  res.json({
    status: 'success',
    users: sessionUsers[req.params.sessionId] || [String(req.user.id)],
  });
});

app.get('/api/collaboration/sessions/:sessionId/history', authenticateToken, (req, res) => {
  res.json({
    status: 'success',
    history: sessionHistory[req.params.sessionId] || [],
    count: (sessionHistory[req.params.sessionId] || []).length,
  });
});

app.get('/api/system/resources', (_req, res) => {
  res.json({
    cpu: 34.2,
    memory: 58.6,
    gpu: 12.4,
    disk: 41.7,
  });
});

app.get('/health', (_req, res) => {
  res.json({ status: 'UP', service: 'FoodSecurityNet Demo API' });
});

app.listen(PORT, () => {
  console.log(`FoodSecurityNet mock API running at http://localhost:${PORT}`);
  console.log('Demo login: demo@foodsecuritynet.org / Demo123!');
});
