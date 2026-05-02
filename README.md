# FoodSecurityNet

FoodSecurityNet is a public-interest food security software project. Its mission is to give nonprofits, universities, researchers, students, civic technologists, and community organizations a free or low-cost tool they can run, inspect, modify, and adapt for agricultural data analysis, food security research, and mission-driven planning.

This repository exists because many teams working on hunger, farming, climate resilience, and community support cannot afford expensive proprietary software. FoodSecurityNet is intended to reduce those software costs while keeping the code and deployment path visible.

## The Problem

Food security work often depends on tools that are expensive, fragmented, or difficult to audit. A nonprofit or university team may need to collect agricultural data, compare environmental and socioeconomic factors, explore results visually, collaborate with partners, and test AI-assisted recommendations without paying for a closed commercial platform.

That creates several practical problems:

- Small teams need affordable software for upload, analysis, visualization, and collaboration workflows.
- Researchers and students need code they can inspect and adapt for experiments.
- Public-interest teams need privacy and security controls they can evaluate before using real participant, farm, community, or institutional data.
- Accessibility matters because public-sector and education deployments often need to support keyboard and assistive-technology users.
- AI outputs need to be treated as advisory, not as final agronomic, legal, health, financial, or public-policy decisions.
- Infrastructure teams need a starting point for Docker, Kubernetes, PostgreSQL, Redis, monitoring, and gateway-based routing, while still being able to review and harden it themselves.

## What This Solves

FoodSecurityNet provides a multi-service starter platform for food security and agricultural analysis workflows:

- A React frontend for login, profile, MFA flows, dataset upload, analysis, exploration, troubleshooting, collaboration, and export actions.
- Spring Boot backend services for authentication, API routing, data integration, visualization support, collaboration sessions, user sessions, and LLM query handling.
- Python FastAPI services for agricultural prediction and LLM-related processing.
- PostgreSQL and Redis configuration for persistent data, sessions, collaboration, rate limiting, and caching.
- Docker Compose and Kubernetes manifests that show how the services are intended to run together.
- A local mock API server so the frontend can be reviewed without starting the full backend stack.

The project does not claim to replace professional agricultural, nutrition, legal, medical, financial, educational, or public-sector review. It gives teams a transparent software base they can adapt and evaluate.

## Who It Is For

- Nonprofits working on hunger, agriculture, climate adaptation, food distribution, or community resilience.
- Universities and research labs studying food security, farming systems, environmental risk, or public-interest technology.
- Researchers and students who need inspectable code for prototypes, class projects, pilots, or reproducible experiments.
- Public-interest teams and civic technologists building tools for community planning or resource coordination.
- Community organizations that need lower-cost software they can run locally or adapt with technical help.
- Individual farmers or advisors experimenting with local data workflows, with appropriate professional judgment.

## Free / Low-Cost Use

FoodSecurityNet is licensed under the MIT License. The MIT License permits free use, copying, modification, merging, publishing, distribution, sublicensing, and sale of copies of the software, provided the copyright notice and permission notice are included in copies or substantial portions of the software.

Contact information present in the repository: `sekacorn@gmail.com` is listed in [`SECURITY.md`](SECURITY.md) for private vulnerability reports. No separate support SLA or funding contact file is present.

Public-interest users can clone, fork, run, inspect, modify, and deploy the project under the MIT License. Organizations should still review third-party dependency licenses, dataset licenses, privacy duties, and deployment obligations before using it with real data.

## What Is Included

- Frontend: React 18, Vite 5, React Router, Tailwind CSS, Axios, Plotly, Three.js, React Three Fiber, STOMP over SockJS, and React Toastify.
- Frontend workflows: home, login, registration, profile, MFA setup/disable UI, analysis, explore, collaboration, troubleshooting chat, route announcements, skip navigation, error boundary, resource monitor, annotation tool, and export UI.
- Backend API gateway: Spring Cloud Gateway with routing, JWT filter, CORS configuration, Redis-backed rate limiting, fallback handling, security headers, actuator health, metrics, and Prometheus endpoints.
- Auth service: registration, login, JWT handling, refresh/logout paths, profile update, MFA-related DTOs and services, OAuth2/SAML-oriented configuration, password policy settings, and auth tests.
- Agri integrator service: agricultural, environmental, and socioeconomic data models, repositories, parsing, validation, upload controller, and CSV/JSON utilities.
- Agri visualizer service: visualization models, controller paths, AI prediction client service, resource monitoring, export controller, and placeholder areas for 3D geometry and image generation.
- User session service: sessions and annotations models, repositories, controllers, and services.
- Collaboration service: collaboration session models, repositories, WebSocket/STOMP configuration, session APIs, and user action history.
- LLM service: query and troubleshooting controllers, LLM query/response/error models, database repositories, OpenAI-style API configuration, and mock fallback responses when no API key is configured.
- AI model service: FastAPI prediction service, PyTorch model code, data processor, trainer, synthetic sample-data generation, health/metrics/resource endpoints, Dockerfile, quickstart, testing notes, and sample request.
- Python LLM service: FastAPI/Transformers/LangChain/OpenAI dependencies and service files under `ai-model/llm-service`.
- Data layer: PostgreSQL schema with users, MFA backup codes, sessions, agricultural data, environmental data, socioeconomic data, recommendations, visualizations, annotations, collaboration sessions, LLM queries, error logs, and audit logs.
- Deployment files: root `docker-compose.yml`, service Dockerfiles, NGINX config, Redis config, and Kubernetes manifests under `infra/kubernetes`.
- Tests: auth-service Java tests and Python end-to-end test scaffolding under `tests/e2e`.
- Docs/assets: service READMEs, AI quickstart/testing docs, compliance readiness notes, privacy notes, security policy, contribution guide, production readiness checklist, and screenshots under `docs/screenshots`.

Current limitations are also visible in the repo: the mock server uses demo credentials and hard-coded demo data, the AI trainer uses synthetic labels/data for sample artifacts, the Java LLM service can return mock responses, some visualization generation paths are placeholders, and production hardening is incomplete.

## Compliance and Trust Posture

FoodSecurityNet is compliance-readiness oriented, not certified. The repo includes [`docs/COMPLIANCE_READINESS.md`](docs/COMPLIANCE_READINESS.md), [`SECURITY.md`](SECURITY.md), [`PRIVACY.md`](PRIVACY.md), and [`docs/PRODUCTION_READINESS_CHECKLIST.md`](docs/PRODUCTION_READINESS_CHECKLIST.md). These files are meant to help deployers evaluate risk and prepare for review; they are not certification claims.

GDPR may matter because the platform can store user accounts, emails, sessions, uploaded agricultural data, location-like fields, collaboration history, annotations, LLM queries, and audit logs. Current support includes inspectable database tables, JWT authentication, MFA-oriented flows, SSO-oriented configuration, and some logging/audit schema support. Deployers still need a lawful basis, data minimization rules, retention/deletion procedures, processor agreements, consent or notice language, data subject request handling, cross-border transfer review, and production privacy review.

European Accessibility Act / EN 301 549 may matter for public-facing or public-sector digital services in Europe. Current support includes skip navigation, route announcements, navigation semantics, reduced-motion CSS, accessible MFA dialog work, live status semantics, and an error boundary. Deployers still need manual keyboard testing, screen-reader review, color-contrast validation, form error association review, and documented accessibility conformance work.

EU AI Act may matter because the project includes AI-assisted agricultural prediction and LLM troubleshooting. Current support is transparent source code, model service docs, sample payloads, health/metrics endpoints, and visible mock/synthetic-data behavior. Deployers still need to classify their AI use case, document datasets and model behavior, evaluate risks, monitor outputs, disclose AI use where required, and keep humans responsible for final decisions.

NIS2 may matter if an organization uses this platform in essential or important service operations. Current support includes gateway routing, rate limiting, security headers, health checks, metrics, Redis/PostgreSQL infrastructure, and deployment manifests. Deployers still need organizational risk management, incident response, backup/restore, vulnerability management, supplier review, access reviews, and operational monitoring.

The Cyber Resilience Act may matter if the software is distributed as a digital product in the EU. Current support includes source availability, dependency files, container files, and some security-oriented controls. Deployers or distributors still need secure development lifecycle evidence, vulnerability disclosure, SBOM/dependency review, patch management, default configuration review, and release security procedures.

Section 508 / WCAG 2.1 AA may matter for US public-sector, education, and grant-funded deployments. Current support overlaps with the accessibility work listed above and in the compliance readiness document. Deployers still need a full accessibility audit and remediation before claiming conformance.

NIST SP 800-53 and the NIST Cybersecurity Framework may matter for universities, public-sector teams, and federally adjacent deployments. Current support includes authentication flows, password policy settings, MFA-oriented code, gateway controls, rate limiting, security headers, actuator/Prometheus metrics, audit-log schema, and service separation. Deployers still need a control baseline, system security plan, access control procedures, vulnerability scanning, log retention, backup procedures, configuration management, and incident response documentation.

FedRAMP readiness may matter only if a team wants to adapt this for federal cloud use. This repository is not FedRAMP authorized and does not claim FedRAMP compliance. It can only be treated as early planning material because it includes containerization, service separation, metrics, and some security controls. A real FedRAMP path would require an authorized cloud boundary, SSP, continuous monitoring, vulnerability scanning, hardened infrastructure, formal policies, and third-party assessment.

## Current Status

FoodSecurityNet should be treated as a prototype / starter kit for research, education, and public-interest software development. It is useful for local demos, UI review, architecture experiments, AI service experimentation, and early compliance-readiness planning.

It is not production-ready without review. Known limitations include demo credentials in `demo-server.js`, mock LLM fallback behavior, synthetic AI training data, placeholder visualization generation code, incomplete production security and privacy procedures, no documented SBOM or dependency scanning workflow, incomplete accessibility audit evidence, and configuration differences across some Docker Compose and service `application.yml` ports. Any deployment using real people, farm, location, institutional, or sensitive community data needs technical, legal, privacy, security, accessibility, and domain review first.

## Quick Start

The simplest working path is the frontend with the local mock API.

1. Install root mock API dependencies:

   ```bash
   npm install
   ```

2. Start the mock API from the repository root:

   ```bash
   node demo-server.js
   ```

   The mock API runs at `http://localhost:8080`.

3. In another terminal, start the frontend:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   The frontend runs at `http://127.0.0.1:3000`.

4. Use the demo login:

   ```text
   Email: demo@foodsecuritynet.org
   Password: Demo123!
   ```

For the AI model service only:

```bash
cd ai-model
pip install -r requirements.txt
python trainer.py
python agri_predictor.py
```

Then test:

```bash
curl http://localhost:8000/health
curl -X POST http://localhost:8000/predict -H "Content-Type: application/json" -d @sample_request.json
```

For full-stack container work, review `.env.example`, then use Docker Compose:

```bash
docker-compose up --build
```

The full stack may need local adjustment before it runs cleanly, especially in CPU-only environments or where service port mappings differ from individual service configuration.

## Project Structure

```text
.
|-- README.md
|-- LICENSE
|-- SECURITY.md
|-- PRIVACY.md
|-- CONTRIBUTING.md
|-- .env.example
|-- demo-server.js
|-- docker-compose.yml
|-- frontend/                  React/Vite web app
|-- backend/
|   |-- api-gateway/           Spring Cloud Gateway
|   |-- auth-service/          Auth, profile, MFA, SSO-oriented config
|   |-- agri-integrator/       Upload, parsing, validation, agri data models
|   |-- agri-visualizer/       Visualization, export, resource monitoring
|   |-- user-session/          Sessions and annotations
|   |-- collaboration-service/ STOMP/WebSocket collaboration
|   `-- llm-service/           Java LLM query and troubleshooting service
|-- ai-model/                  Python FastAPI/PyTorch prediction service
|-- database/                  PostgreSQL schema and Redis config
|-- docs/                      Compliance notes and screenshots
|-- infra/                     Kubernetes and NGINX deployment files
`-- tests/                     Python E2E test scaffold
```

## Testing

Commands present in the repository include:

```bash
cd frontend
npm run build
npm run lint
```

```bash
cd backend/auth-service
mvn test
```

```bash
cd tests/e2e
pip install -r requirements.txt
pytest
```

On Windows, the repository also includes:

```bat
tests\run_e2e_tests.bat
```

For the AI model service, use the smoke checks in [`ai-model/TESTING.md`](ai-model/TESTING.md). The root README previously noted that the frontend build had been verified locally, but Maven-based Java compilation was not verified in that environment. Re-run tests in your own environment before trusting the system.

## License

See [`LICENSE`](LICENSE). FoodSecurityNet is licensed under the MIT License, which allows broad free use, modification, and distribution when the copyright and permission notices are included.
