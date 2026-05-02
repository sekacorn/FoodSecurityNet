# Privacy Notes

FoodSecurityNet is intended for nonprofits, universities, researchers, students, civic technologists, and public-interest teams that need lower-cost food security software. It can be adapted for privacy-conscious deployments, but this repository is not a complete privacy program by itself.

## Data The Project Can Handle

Based on the current code and database schema, deployments may handle:

- User account data such as username, email, full name, role, login timestamps, MFA state, and SSO identifiers.
- Authentication data such as password hashes, JWTs, refresh tokens, MFA secrets, and backup code hashes.
- Agricultural data such as crop type, yield, soil attributes, irrigation type, planting/harvest dates, region, country, latitude, and longitude.
- Environmental data such as temperature, rainfall, humidity, wind speed, solar radiation, climate zone, and observation dates.
- Socioeconomic data such as income level, market access score, farm size, household size, education level, infrastructure score, and food security index.
- Visualizations, annotations, collaboration sessions, user actions, LLM queries, LLM responses, error logs, and audit logs.

Some of this data may be personal, sensitive, location-linked, institutionally sensitive, or community-sensitive depending on deployment context.

## Demo And Mock Data

The local mock API in `demo-server.js` uses demo credentials and hard-coded sample data. It is for local review only and should not be exposed as a production service.

The AI model training path includes synthetic sample-data generation. Synthetic demo data should not be treated as validated agronomic evidence.

## GDPR And Privacy Review

GDPR may apply if the system processes personal data of people in the European Economic Area or is used by organizations subject to EU privacy obligations.

Current repository support:

- Inspectable source code and database schema.
- Account, session, audit, and collaboration tables.
- Environment-driven configuration for secrets and identity providers.
- Authentication and MFA-oriented workflows.

Deployment responsibilities:

- Identify controller/processor roles.
- Establish a lawful basis for processing.
- Publish privacy notices.
- Minimize collected data.
- Configure retention and deletion workflows.
- Support access, correction, export, and deletion requests where required.
- Review cross-border transfers and subprocessors.
- Protect backups, logs, and analytics data.
- Complete a DPIA where risk requires it.

## AI And Human Review

FoodSecurityNet includes AI-assisted prediction and troubleshooting components. AI outputs should be treated as advisory. Professional or organizational reviewers remain responsible for final decisions, especially in agricultural planning, food distribution, public-sector, educational, legal, medical, financial, or community-impact contexts.

Deployers should document:

- Which AI models are used.
- What data is sent to each model or external API.
- Whether outputs are shown as AI-assisted.
- Who reviews or approves important decisions.
- How incorrect or harmful outputs are reported and corrected.

## Operational Privacy Checklist

Before using real data, review:

- Secrets and API keys.
- Data retention periods.
- Log content and log retention.
- Access roles and admin permissions.
- Backup encryption and restore testing.
- Vendor and cloud provider agreements.
- User consent or notice requirements.
- Data export and deletion processes.
- Dataset licensing and attribution.
