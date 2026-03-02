# Testing Guide for AI Model Service

## Quick Test Commands

### 1. Test Health Endpoint

```bash
curl http://localhost:8000/health
```

Expected response:
```json
{
  "status": "healthy",
  "model_loaded": true,
  "version": "1.0.0",
  "timestamp": "2025-10-26T10:30:00"
}
```

### 2. Test Resource Check

```bash
curl http://localhost:8000/resources/check
```

Expected response:
```json
{
  "cpu_cores": 8,
  "cpu_percent": 15.2,
  "memory_total_gb": 16.0,
  "memory_available_gb": 8.5,
  "memory_percent": 46.9,
  "gpu_available": false,
  "multiprocessing_enabled": true
}
```

### 3. Test Prediction Endpoint

#### Using curl (Linux/Mac):
```bash
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d @sample_request.json
```

#### Using curl (Windows PowerShell):
```powershell
curl.exe -X POST http://localhost:8000/predict `
  -H "Content-Type: application/json" `
  -d "@sample_request.json"
```

#### Using Python:
```python
import requests
import json

with open('sample_request.json', 'r') as f:
    data = json.load(f)

response = requests.post(
    'http://localhost:8000/predict',
    json=data
)

print(json.dumps(response.json(), indent=2))
```

### 4. Test Metrics Endpoint

```bash
curl http://localhost:8000/metrics
```

## Full Integration Test Script

Create `test_api.py`:

```python
import requests
import json
import time

BASE_URL = "http://localhost:8000"

def test_health():
    """Test health endpoint"""
    print("Testing /health endpoint...")
    response = requests.get(f"{BASE_URL}/health")
    assert response.status_code == 200
    data = response.json()
    assert data['status'] == 'healthy'
    print("✓ Health check passed")

def test_resources():
    """Test resources endpoint"""
    print("\nTesting /resources/check endpoint...")
    response = requests.get(f"{BASE_URL}/resources/check")
    assert response.status_code == 200
    data = response.json()
    assert 'cpu_cores' in data
    assert 'memory_total_gb' in data
    print(f"✓ Resources check passed")
    print(f"  CPU Cores: {data['cpu_cores']}")
    print(f"  Memory: {data['memory_total_gb']:.2f} GB")
    print(f"  GPU Available: {data['gpu_available']}")
    print(f"  Multiprocessing: {data['multiprocessing_enabled']}")

def test_prediction():
    """Test prediction endpoint"""
    print("\nTesting /predict endpoint...")

    with open('sample_request.json', 'r') as f:
        data = json.load(f)

    start_time = time.time()
    response = requests.post(f"{BASE_URL}/predict", json=data)
    duration = time.time() - start_time

    assert response.status_code == 200
    result = response.json()

    assert 'crop_recommendations' in result
    assert 'irrigation_recommendations' in result
    assert 'fertilization_recommendations' in result
    assert 'overall_strategy' in result
    assert 'confidence_score' in result

    print("✓ Prediction passed")
    print(f"  Response time: {duration*1000:.2f}ms")
    print(f"  Confidence: {result['confidence_score']:.2%}")
    print(f"  Top crop: {result['crop_recommendations'][0]['strategy']}")
    print(f"  MBTI tailored: {result['mbti_tailored']}")

def test_prediction_without_mbti():
    """Test prediction without MBTI type"""
    print("\nTesting /predict without MBTI...")

    with open('sample_request.json', 'r') as f:
        data = json.load(f)

    # Remove MBTI type
    data['socioeconomic']['mbti_type'] = None

    response = requests.post(f"{BASE_URL}/predict", json=data)
    assert response.status_code == 200
    result = response.json()
    assert result['mbti_tailored'] == False
    print("✓ Prediction without MBTI passed")

def test_prediction_validation():
    """Test input validation"""
    print("\nTesting input validation...")

    # Invalid soil pH
    invalid_data = {
        "agricultural": {
            "farm_size": 10.5,
            "soil_ph": 15.0,  # Invalid: > 14
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
            "access_to_credit": True,
            "extension_services": True
        }
    }

    response = requests.post(f"{BASE_URL}/predict", json=invalid_data)
    assert response.status_code == 422  # Validation error
    print("✓ Validation test passed")

def test_metrics():
    """Test metrics endpoint"""
    print("\nTesting /metrics endpoint...")
    response = requests.get(f"{BASE_URL}/metrics")
    assert response.status_code == 200
    metrics = response.text
    assert 'predictions_total' in metrics
    print("✓ Metrics endpoint passed")

def run_all_tests():
    """Run all tests"""
    print("=" * 60)
    print("AI Model Service - Integration Tests")
    print("=" * 60)

    try:
        test_health()
        test_resources()
        test_prediction()
        test_prediction_without_mbti()
        test_prediction_validation()
        test_metrics()

        print("\n" + "=" * 60)
        print("All tests passed! ✓")
        print("=" * 60)
    except AssertionError as e:
        print(f"\n✗ Test failed: {e}")
        return False
    except requests.exceptions.ConnectionError:
        print("\n✗ Connection error. Is the service running?")
        print("Start the service with: python agri_predictor.py")
        return False
    except Exception as e:
        print(f"\n✗ Unexpected error: {e}")
        return False

    return True

if __name__ == '__main__':
    run_all_tests()
```

## Performance Testing

### Load Test with Apache Bench

```bash
# Install Apache Bench
# Ubuntu: sudo apt-get install apache2-utils
# Mac: brew install httpd

# Run load test (100 requests, 10 concurrent)
ab -n 100 -c 10 -p sample_request.json -T application/json \
  http://localhost:8000/predict
```

### Load Test with Python

```python
import concurrent.futures
import requests
import json
import time

def make_prediction():
    with open('sample_request.json', 'r') as f:
        data = json.load(f)

    start = time.time()
    response = requests.post('http://localhost:8000/predict', json=data)
    duration = time.time() - start

    return {
        'status_code': response.status_code,
        'duration': duration
    }

# Run 100 requests with 10 workers
with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
    futures = [executor.submit(make_prediction) for _ in range(100)]
    results = [f.result() for f in concurrent.futures.as_completed(futures)]

# Calculate statistics
durations = [r['duration'] for r in results]
success_count = sum(1 for r in results if r['status_code'] == 200)

print(f"Success rate: {success_count/len(results)*100:.1f}%")
print(f"Average latency: {sum(durations)/len(durations)*1000:.2f}ms")
print(f"Min latency: {min(durations)*1000:.2f}ms")
print(f"Max latency: {max(durations)*1000:.2f}ms")
```

## Docker Testing

### Test Docker Build

```bash
# Build image
docker build -t farming-ai-model:test .

# Verify image size
docker images farming-ai-model:test

# Test container
docker run -p 8000:8000 --name ai-test farming-ai-model:test

# In another terminal
curl http://localhost:8000/health

# Cleanup
docker stop ai-test
docker rm ai-test
```

### Test with Docker Compose

Create `docker-compose.test.yml`:

```yaml
version: '3.8'

services:
  ai-model:
    build: .
    ports:
      - "8000:8000"
    environment:
      - LOG_LEVEL=debug
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

Run:
```bash
docker-compose -f docker-compose.test.yml up -d
docker-compose -f docker-compose.test.yml ps
docker-compose -f docker-compose.test.yml logs
docker-compose -f docker-compose.test.yml down
```

## Continuous Testing

### Monitor Service Health

```bash
# Continuously monitor health
watch -n 5 curl -s http://localhost:8000/health

# Monitor metrics
watch -n 5 curl -s http://localhost:8000/metrics | grep predictions_total
```

## Troubleshooting

### Common Issues

1. **Service won't start**
   ```bash
   # Check logs
   python agri_predictor.py

   # Check dependencies
   pip list | grep torch
   ```

2. **Model not found**
   ```bash
   # Train model
   python trainer.py

   # Verify model file
   ls -lh model.pt
   ```

3. **Slow predictions**
   ```bash
   # Check resource usage
   curl http://localhost:8000/resources/check

   # Check metrics
   curl http://localhost:8000/metrics | grep prediction_duration
   ```

4. **Memory issues**
   ```bash
   # Reduce batch size in config.py
   BATCH_SIZE = 16  # Instead of 32
   ```

## Expected Performance Benchmarks

- Health check: < 10ms
- Resource check: < 50ms
- Prediction (CPU): 50-200ms
- Prediction (GPU): 10-50ms
- Cold start: < 5s
- Memory usage: 200-500MB
- Throughput: 10-50 req/s (CPU), 50-200 req/s (GPU)
