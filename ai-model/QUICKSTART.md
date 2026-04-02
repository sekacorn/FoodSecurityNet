# Quick Start Guide

This guide is the shortest path to a working local AI model service. For endpoint details, configuration, and architecture, use [README.md](README.md). For test procedures, use [TESTING.md](TESTING.md).

## Option 1: Automatic Setup

**Linux/Mac**
```bash
chmod +x setup.sh
./setup.sh
```

**Windows**
```cmd
setup.bat
```

## Option 2: Manual Setup

```bash
# Create a virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# OR
venv\Scripts\activate.bat  # Windows

# Install dependencies
pip install -r requirements.txt

# Train the sample model artifacts
python trainer.py
```

## Run the Service

**Development**
```bash
python agri_predictor.py
```

**Production-style**
```bash
uvicorn agri_predictor:app --host 0.0.0.0 --port 8000 --workers 4
```

**Docker**
```bash
docker build -t farming-ai-model .
docker run -p 8000:8000 farming-ai-model
```

## Smoke Test

```bash
curl http://localhost:8000/health
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d @sample_request.json
```

Open [http://localhost:8000/docs](http://localhost:8000/docs) for interactive API docs.

## Where to Go Next

- Endpoint reference and env vars: [README.md](README.md)
- Sample payload: [sample_request.json](sample_request.json)
- Integration and load testing: [TESTING.md](TESTING.md)
