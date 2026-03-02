# Quick Start Guide

## Installation (3 steps)

### Option 1: Automatic Setup (Recommended)

**Linux/Mac:**
```bash
chmod +x setup.sh
./setup.sh
```

**Windows:**
```cmd
setup.bat
```

### Option 2: Manual Setup

```bash
# 1. Create virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# OR
venv\Scripts\activate.bat  # Windows

# 2. Install dependencies
pip install -r requirements.txt

# 3. Train model (generates model.pt)
python trainer.py
```

## Running the Service

### Development Mode
```bash
python agri_predictor.py
```

### Production Mode
```bash
uvicorn agri_predictor:app --host 0.0.0.0 --port 8000 --workers 4
```

### Docker
```bash
docker build -t farming-ai-model .
docker run -p 8000:8000 farming-ai-model
```

## Testing (30 seconds)

```bash
# 1. Check health
curl http://localhost:8000/health

# 2. Make prediction
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d @sample_request.json

# 3. View API docs
# Open browser: http://localhost:8000/docs
```

## Key Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/predict` | POST | Get farming recommendations |
| `/health` | GET | Check service health |
| `/metrics` | GET | Prometheus metrics |
| `/resources/check` | GET | System resources |
| `/docs` | GET | Interactive API documentation |

## Sample Request

```json
{
  "agricultural": {
    "farm_size": 10.5,
    "soil_ph": 6.5,
    "soil_nitrogen": 45,
    "soil_phosphorus": 30,
    "soil_potassium": 25,
    "irrigation_frequency": 3,
    "soil_type": "loam",
    "current_crop": "maize"
  },
  "environmental": {
    "rainfall": 800,
    "temperature": 25,
    "humidity": 60,
    "elevation": 1200,
    "region": "east"
  },
  "socioeconomic": {
    "income": 50000,
    "distance_to_market": 15,
    "education_level": 3,
    "access_to_credit": true,
    "extension_services": true,
    "mbti_type": "INTJ"
  }
}
```

## Configuration

Create `.env` file (copy from `.env.example`):

```env
MODEL_PATH=model.pt
PORT=8000
LOG_LEVEL=info
```

## Troubleshooting

**Service won't start?**
- Check if port 8000 is available
- Ensure all dependencies are installed: `pip install -r requirements.txt`

**Model not found?**
- Run training script: `python trainer.py`

**Slow predictions?**
- Check resources: `curl http://localhost:8000/resources/check`
- Use GPU if available
- Enable multiprocessing (requires 4+ CPU cores, 8GB+ RAM)

## Next Steps

1. Read [README.md](README.md) for detailed documentation
2. Check [TESTING.md](TESTING.md) for comprehensive tests
3. Explore interactive docs at http://localhost:8000/docs
4. Customize model in `model.py`
5. Add real training data in `trainer.py`

## Support

- API Documentation: http://localhost:8000/docs
- Health Check: http://localhost:8000/health
- Metrics: http://localhost:8000/metrics
