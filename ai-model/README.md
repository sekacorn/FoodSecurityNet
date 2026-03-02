# AI Model Service for Farming Predictions

PyTorch-based AI service for agricultural recommendations with FastAPI.

## Features

- **PyTorch Neural Network**: Deep learning model for farming predictions
- **FastAPI Service**: RESTful API with automatic documentation
- **Multi-task Predictions**: Crop selection, irrigation methods, fertilization strategies
- **MBTI-Tailored Recommendations**: Personalized advice based on personality types
- **Resource-Aware**: Automatic multiprocessing if CPU > 4 cores and RAM > 8GB
- **Monitoring**: Prometheus metrics for production monitoring
- **Docker Support**: Multi-stage Docker build for deployment

## Quick Start

### 1. Install Dependencies

```bash
pip install -r requirements.txt
```

### 2. Train Model (Optional)

Generate a pretrained model with synthetic data:

```bash
python trainer.py
```

This creates:
- `model.pt` - Trained PyTorch model
- `data_processor.pkl` - Fitted data preprocessor

### 3. Run Service

```bash
python agri_predictor.py
```

Or with uvicorn:

```bash
uvicorn agri_predictor:app --host 0.0.0.0 --port 8000
```

### 4. Using Docker

Build:
```bash
docker build -t farming-ai-model .
```

Run:
```bash
docker run -p 8000:8000 farming-ai-model
```

## API Endpoints

### POST /predict
Make farming predictions

**Request Body:**
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

**Response:**
```json
{
  "crop_recommendations": [
    {
      "strategy": "maize",
      "confidence": 0.85,
      "reasoning": "Based on soil conditions and climate patterns"
    }
  ],
  "irrigation_recommendations": [...],
  "fertilization_recommendations": [...],
  "overall_strategy": "Recommended: maize with drip irrigation",
  "confidence_score": 0.85,
  "mbti_tailored": true,
  "timestamp": "2025-10-26T10:30:00"
}
```

### GET /health
Health check endpoint

### GET /metrics
Prometheus metrics for monitoring

### GET /resources/check
System resource information

## Configuration

Create a `.env` file:

```env
MODEL_PATH=model.pt
MODEL_VERSION=1.0.0
HOST=0.0.0.0
PORT=8000
WORKERS=1
LOG_LEVEL=info
MIN_CPU_CORES_FOR_MULTIPROCESSING=4
MIN_MEMORY_GB_FOR_MULTIPROCESSING=8
```

## Model Architecture

**FarmingRecommendationModel:**
- Input Layer: 50 features (agricultural + environmental + socioeconomic)
- Hidden Layers: [256, 128, 64, 32] with ReLU, BatchNorm, Dropout
- Output Layer: 15 strategy scores
- Activation: Softmax for probabilities

**MultiTaskFarmingModel:**
- Shared feature extraction network
- Separate prediction heads for:
  - Crop selection (8 classes)
  - Irrigation methods (5 classes)
  - Fertilization strategies (5 classes)

## MBTI Personality Integration

The service tailors recommendations based on MBTI types:

- **INTJ/INTP**: Innovation-focused, data-driven approaches
- **ISTJ/ISFJ**: Traditional, proven methods
- **ENFP/INFP**: Sustainability-focused strategies
- **ESTP/ISTP**: Hands-on, practical solutions

## Data Processing Pipeline

1. **Feature Engineering**: Create derived features (soil health index, moisture index, etc.)
2. **Missing Value Imputation**: Median for numerical, mode for categorical
3. **Encoding**: Label encoding for categorical variables
4. **Scaling**: StandardScaler for numerical features
5. **Validation**: Pydantic models ensure data quality

## Monitoring

Prometheus metrics available at `/metrics`:

- `predictions_total`: Total predictions made
- `prediction_duration_seconds`: Prediction latency
- `active_requests`: Concurrent requests
- `cpu_usage_percent`: CPU utilization
- `memory_usage_percent`: Memory utilization

## Development

### Project Structure

```
ai-model/
├── agri_predictor.py      # FastAPI service
├── model.py               # PyTorch model architectures
├── data_processor.py      # Data preprocessing
├── trainer.py             # Training pipeline
├── config.py              # Configuration
├── requirements.txt       # Dependencies
├── Dockerfile             # Docker build
├── .dockerignore         # Docker ignore patterns
└── README.md             # Documentation
```

### Testing

```bash
# Test prediction endpoint
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d @sample_request.json

# Check health
curl http://localhost:8000/health

# Check resources
curl http://localhost:8000/resources/check
```

## Production Deployment

1. Train model with real data:
   ```bash
   python trainer.py
   ```

2. Build Docker image:
   ```bash
   docker build -t farming-ai-model:v1.0.0 .
   ```

3. Deploy with resource limits:
   ```bash
   docker run -d \
     --name farming-ai \
     -p 8000:8000 \
     --memory=4g \
     --cpus=2 \
     -v $(pwd)/model.pt:/app/model.pt \
     -v $(pwd)/data_processor.pkl:/app/data_processor.pkl \
     farming-ai-model:v1.0.0
   ```

4. Setup monitoring with Prometheus:
   - Scrape `/metrics` endpoint
   - Set up alerts for high latency or errors

## License

MIT License
