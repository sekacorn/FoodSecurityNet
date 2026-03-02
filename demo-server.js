// Simple Node.js mock server for FoodSecurityNet demo
const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const jwt = require('jsonwebtoken');

const app = express();
const PORT = 8080;
const JWT_SECRET = 'demo-secret-key';

// Middleware
app.use(cors());
app.use(bodyParser.json());

// In-memory data store
let users = [
  {
    id: 1,
    username: 'demo',
    email: 'demo@foodsecuritynet.org',
    password: 'Demo123!', // In production, this would be hashed
    firstName: 'Demo',
    lastName: 'User',
    mbtiType: 'INTJ',
    role: 'USER',
    mfaEnabled: false,
    emailVerified: true
  }
];

let sessions = [];
let annotations = [];

// Helper function to generate JWT
const generateToken = (user) => {
  return jwt.sign(
    {
      id: user.id,
      username: user.username,
      email: user.email,
      role: user.role,
      mbtiType: user.mbtiType
    },
    JWT_SECRET,
    { expiresIn: '24h' }
  );
};

// Authentication middleware
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
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

// ===== AUTH ENDPOINTS =====

// Login
app.post('/api/auth/login', (req, res) => {
  const { username, password } = req.body;

  const user = users.find(u => u.username === username && u.password === password);

  if (!user) {
    return res.status(401).json({ message: 'Invalid username or password' });
  }

  const token = generateToken(user);

  res.json({
    token,
    refreshToken: `refresh_${token}`,
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      mbtiType: user.mbtiType,
      role: user.role,
      mfaEnabled: user.mfaEnabled,
      emailVerified: user.emailVerified
    }
  });
});

// Register
app.post('/api/auth/register', (req, res) => {
  const { username, email, password, firstName, lastName, mbtiType } = req.body;

  if (users.find(u => u.username === username || u.email === email)) {
    return res.status(400).json({ message: 'Username or email already exists' });
  }

  const newUser = {
    id: users.length + 1,
    username,
    email,
    password,
    firstName,
    lastName,
    mbtiType: mbtiType || 'INTJ',
    role: 'USER',
    mfaEnabled: false,
    emailVerified: true
  };

  users.push(newUser);
  const token = generateToken(newUser);

  res.status(201).json({
    token,
    refreshToken: `refresh_${token}`,
    user: {
      id: newUser.id,
      username: newUser.username,
      email: newUser.email,
      firstName: newUser.firstName,
      lastName: newUser.lastName,
      mbtiType: newUser.mbtiType,
      role: newUser.role,
      mfaEnabled: newUser.mfaEnabled,
      emailVerified: newUser.emailVerified
    }
  });
});

// Get current user
app.get('/api/auth/me', authenticateToken, (req, res) => {
  const user = users.find(u => u.id === req.user.id);
  if (!user) {
    return res.status(404).json({ message: 'User not found' });
  }

  res.json({
    id: user.id,
    username: user.username,
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    mbtiType: user.mbtiType,
    role: user.role,
    mfaEnabled: user.mfaEnabled,
    emailVerified: user.emailVerified
  });
});

// ===== DATA UPLOAD ENDPOINTS =====

app.post('/api/agri-integrator/upload/csv', authenticateToken, (req, res) => {
  // Simulate CSV upload
  res.json({
    message: 'CSV data uploaded successfully',
    recordsProcessed: 150,
    dataType: 'agricultural'
  });
});

app.post('/api/agri-integrator/upload/json', authenticateToken, (req, res) => {
  // Simulate JSON upload
  res.json({
    message: 'JSON data uploaded successfully',
    recordsProcessed: 75,
    dataType: 'environmental'
  });
});

// ===== SESSION ENDPOINTS =====

app.get('/api/user-session/sessions', authenticateToken, (req, res) => {
  const userSessions = sessions.filter(s => s.userId === req.user.id);
  res.json(userSessions);
});

app.post('/api/user-session/sessions', authenticateToken, (req, res) => {
  const { name, description } = req.body;

  const newSession = {
    id: sessions.length + 1,
    userId: req.user.id,
    name: name || 'New Session',
    description: description || '',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  sessions.push(newSession);
  res.status(201).json(newSession);
});

// ===== VISUALIZATION ENDPOINTS =====

app.get('/api/agri-visualizer/visualizations/:sessionId', authenticateToken, (req, res) => {
  // Return sample 3D visualization data
  res.json({
    id: 1,
    sessionId: parseInt(req.params.sessionId),
    visualizationType: 'heatmap',
    data: {
      type: 'mesh',
      geometry: 'plane',
      dimensions: { width: 100, height: 100 },
      dataPoints: generateSampleHeatmapData()
    },
    metadata: {
      title: 'Soil Moisture Heatmap',
      region: 'Sample Farm Region',
      date: new Date().toISOString()
    }
  });
});

app.post('/api/agri-visualizer/generate', authenticateToken, (req, res) => {
  res.json({
    id: Math.floor(Math.random() * 1000),
    status: 'completed',
    visualizationType: req.body.type || 'heatmap',
    message: 'Visualization generated successfully'
  });
});

// ===== AI PREDICTION ENDPOINTS =====

app.post('/api/ai/predict', authenticateToken, (req, res) => {
  // Return mock farming predictions
  res.json({
    cropRecommendation: 'Wheat',
    cropConfidence: 0.87,
    irrigationStrategy: 'Drip irrigation every 2 days',
    irrigationConfidence: 0.92,
    fertilizationPlan: 'Nitrogen-rich fertilizer, 50kg per hectare',
    fertilizerConfidence: 0.85,
    yieldPrediction: 4.2,
    yieldUnit: 'tons/hectare',
    riskFactors: ['Drought risk: Medium', 'Pest risk: Low'],
    mbtiPersonalization: `Tailored for ${req.user.mbtiType}: Focus on strategic planning and data-driven decisions`
  });
});

// ===== LLM QUERY ENDPOINTS =====

app.post('/api/llm/query', authenticateToken, (req, res) => {
  const { query } = req.body;

  // Mock LLM responses based on query keywords
  let response = '';

  if (query.toLowerCase().includes('weather') || query.toLowerCase().includes('climate')) {
    response = `Based on current climate data, the region shows moderate temperatures with adequate rainfall. For ${req.user.mbtiType} types, I recommend focusing on long-term climate adaptation strategies and data-backed irrigation planning.`;
  } else if (query.toLowerCase().includes('crop') || query.toLowerCase().includes('plant')) {
    response = `Wheat and corn are excellent crop choices for your region. The AI model suggests wheat with 87% confidence based on soil composition and climate patterns. As an ${req.user.mbtiType}, you might appreciate the systematic approach to crop rotation.`;
  } else if (query.toLowerCase().includes('soil')) {
    response = `Soil analysis indicates loamy texture with pH 6.5, suitable for most crops. Nitrogen levels are adequate, but phosphorus supplementation recommended. The data-driven approach aligns well with ${req.user.mbtiType} decision-making preferences.`;
  } else {
    response = `I'm FoodSecurityNet's AI assistant. I can help with farming predictions, crop recommendations, irrigation strategies, and soil analysis. What specific information would you like to know? (Personalized for ${req.user.mbtiType})`;
  }

  res.json({
    query,
    response,
    confidence: 0.89,
    sources: ['FAO Database', 'NOAA Climate Data', 'Local Soil Reports'],
    mbtiPersonalization: req.user.mbtiType
  });
});

// ===== COLLABORATION ENDPOINTS =====

app.get('/api/collaboration/sessions', authenticateToken, (req, res) => {
  res.json([
    {
      id: 1,
      name: 'Team Planning Session',
      participants: ['demo', 'user2'],
      status: 'active',
      createdAt: new Date().toISOString()
    }
  ]);
});

// ===== ANNOTATION ENDPOINTS =====

app.get('/api/user-session/annotations/:sessionId', authenticateToken, (req, res) => {
  const sessionAnnotations = annotations.filter(a => a.sessionId === parseInt(req.params.sessionId));
  res.json(sessionAnnotations);
});

app.post('/api/user-session/annotations', authenticateToken, (req, res) => {
  const { sessionId, content, position } = req.body;

  const newAnnotation = {
    id: annotations.length + 1,
    sessionId,
    userId: req.user.id,
    content,
    position,
    createdAt: new Date().toISOString()
  };

  annotations.push(newAnnotation);
  res.status(201).json(newAnnotation);
});

// Helper function to generate sample heatmap data
function generateSampleHeatmapData() {
  const data = [];
  for (let x = 0; x < 20; x++) {
    for (let z = 0; z < 20; z++) {
      data.push({
        x: x * 5,
        y: Math.sin(x * 0.5) * Math.cos(z * 0.5) * 10 + 20,
        z: z * 5,
        value: Math.random() * 100
      });
    }
  }
  return data;
}

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'FoodSecurityNet Demo API' });
});

// Start server
app.listen(PORT, () => {
  console.log(`
╔═══════════════════════════════════════════════════════════╗
║        FoodSecurityNet Demo Server Running               ║
╠═══════════════════════════════════════════════════════════╣
║  API Server: http://localhost:${PORT}                        ║
║                                                           ║
║  Test Credentials:                                        ║
║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  ║
║  Username: demo                                           ║
║  Password: Demo123!                                       ║
║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  ║
║                                                           ║
║  Features Available:                                      ║
║  • Login/Registration                                     ║
║  • Data Upload (CSV/JSON)                                 ║
║  • AI Predictions                                         ║
║  • LLM Chat Assistant                                     ║
║  • 3D Visualizations                                      ║
║  • MBTI Personalization (INTJ)                            ║
╚═══════════════════════════════════════════════════════════╝
  `);
});
