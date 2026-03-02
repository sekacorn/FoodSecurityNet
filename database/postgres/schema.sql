-- FoodSecurityNet Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table with SSO support
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255), -- Nullable for SSO users
    full_name VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER', -- USER, MODERATOR, ADMIN, ENTERPRISE
    mbti_type VARCHAR(4), -- 16 MBTI types
    sso_provider VARCHAR(50), -- GOOGLE, MICROSOFT, OKTA, SAML, null for local
    sso_subject VARCHAR(255), -- SSO provider's user ID
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255), -- TOTP secret
    email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    UNIQUE(sso_provider, sso_subject)
);

-- MFA Backup Codes
CREATE TABLE mfa_backup_codes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code_hash VARCHAR(255) NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Sessions
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_data JSONB,
    preferences JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Agricultural Data
CREATE TABLE agricultural_data (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    region VARCHAR(255),
    country VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    crop_type VARCHAR(100),
    crop_yield DECIMAL(10, 2),
    soil_type VARCHAR(100),
    soil_ph DECIMAL(4, 2),
    soil_nitrogen DECIMAL(10, 2),
    soil_phosphorus DECIMAL(10, 2),
    soil_potassium DECIMAL(10, 2),
    irrigation_type VARCHAR(50),
    planting_date DATE,
    harvest_date DATE,
    data_source VARCHAR(100), -- FAO, USDA, USER
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Environmental Data
CREATE TABLE environmental_data (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agricultural_data_id UUID REFERENCES agricultural_data(id) ON DELETE CASCADE,
    region VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    temperature_avg DECIMAL(5, 2),
    temperature_min DECIMAL(5, 2),
    temperature_max DECIMAL(5, 2),
    rainfall DECIMAL(10, 2),
    humidity DECIMAL(5, 2),
    wind_speed DECIMAL(10, 2),
    solar_radiation DECIMAL(10, 2),
    climate_zone VARCHAR(100),
    data_source VARCHAR(100), -- NOAA, USER
    observation_date DATE,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Socio-Economic Data
CREATE TABLE socioeconomic_data (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agricultural_data_id UUID REFERENCES agricultural_data(id) ON DELETE CASCADE,
    region VARCHAR(255),
    country VARCHAR(100),
    income_level VARCHAR(50),
    market_access_score DECIMAL(5, 2),
    farm_size DECIMAL(10, 2),
    household_size INTEGER,
    education_level VARCHAR(50),
    infrastructure_score DECIMAL(5, 2),
    food_security_index DECIMAL(5, 2),
    data_source VARCHAR(100),
    survey_date DATE,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Farming Recommendations
CREATE TABLE farming_recommendations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    agricultural_data_id UUID REFERENCES agricultural_data(id) ON DELETE CASCADE,
    recommendation_type VARCHAR(50), -- CROP_SELECTION, IRRIGATION, FERTILIZATION, PEST_CONTROL
    recommendation_text TEXT NOT NULL,
    confidence_score DECIMAL(5, 2),
    mbti_tailored VARCHAR(4), -- MBTI type this recommendation is tailored for
    ai_model_version VARCHAR(50),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Visualizations
CREATE TABLE visualizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255),
    description TEXT,
    visualization_type VARCHAR(50), -- 3D_MAP, YIELD_MAP, SOIL_MAP
    data_query JSONB,
    visualization_config JSONB,
    thumbnail_url VARCHAR(500),
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Annotations
CREATE TABLE annotations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    visualization_id UUID REFERENCES visualizations(id) ON DELETE CASCADE,
    agricultural_data_id UUID REFERENCES agricultural_data(id) ON DELETE CASCADE,
    annotation_type VARCHAR(50), -- NOTE, HIGHLIGHT, MARKER
    content TEXT,
    position JSONB, -- 3D coordinates or region
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Collaboration Sessions
CREATE TABLE collaboration_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    visualization_id UUID REFERENCES visualizations(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE,
    max_participants INTEGER DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Collaboration Participants
CREATE TABLE collaboration_participants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES collaboration_sessions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) DEFAULT 'PARTICIPANT', -- OWNER, MODERATOR, PARTICIPANT
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(session_id, user_id)
);

-- LLM Queries
CREATE TABLE llm_queries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    query_text TEXT NOT NULL,
    response_text TEXT,
    context JSONB,
    mbti_type VARCHAR(4),
    query_type VARCHAR(50), -- FARMING_ADVICE, TROUBLESHOOTING, VISUALIZATION, GENERAL
    response_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Error Logs
CREATE TABLE error_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    service_name VARCHAR(100),
    error_type VARCHAR(100),
    error_message TEXT,
    stack_trace TEXT,
    context JSONB,
    resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id UUID,
    details JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_sso ON users(sso_provider, sso_subject);
CREATE INDEX idx_agricultural_data_region ON agricultural_data(region, country);
CREATE INDEX idx_agricultural_data_user ON agricultural_data(user_id);
CREATE INDEX idx_environmental_data_agri ON environmental_data(agricultural_data_id);
CREATE INDEX idx_socioeconomic_data_agri ON socioeconomic_data(agricultural_data_id);
CREATE INDEX idx_recommendations_user ON farming_recommendations(user_id);
CREATE INDEX idx_recommendations_agri ON farming_recommendations(agricultural_data_id);
CREATE INDEX idx_visualizations_user ON visualizations(user_id);
CREATE INDEX idx_annotations_user ON annotations(user_id);
CREATE INDEX idx_annotations_viz ON annotations(visualization_id);
CREATE INDEX idx_collab_sessions_owner ON collaboration_sessions(owner_id);
CREATE INDEX idx_collab_participants_session ON collaboration_participants(session_id);
CREATE INDEX idx_llm_queries_user ON llm_queries(user_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_user_sessions_updated_at BEFORE UPDATE ON user_sessions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_agricultural_data_updated_at BEFORE UPDATE ON agricultural_data FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_visualizations_updated_at BEFORE UPDATE ON visualizations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_annotations_updated_at BEFORE UPDATE ON annotations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_collaboration_sessions_updated_at BEFORE UPDATE ON collaboration_sessions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
