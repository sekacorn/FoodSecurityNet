# AI Model Testing

This guide covers quick validation for the standalone AI model service on port `8000`.

## Smoke Checks

```bash
curl http://localhost:8000/health
curl http://localhost:8000/resources/check
curl http://localhost:8000/metrics
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d @sample_request.json
```

Windows PowerShell:

```powershell
curl.exe -X POST http://localhost:8000/predict `
  -H "Content-Type: application/json" `
  -d "@sample_request.json"
```

## Python Smoke Script

```python
import json
import requests
import time

BASE_URL = "http://localhost:8000"

with open("sample_request.json", "r") as f:
    payload = json.load(f)

health = requests.get(f"{BASE_URL}/health")
print("health:", health.status_code)

resources = requests.get(f"{BASE_URL}/resources/check")
print("resources:", resources.status_code)

start = time.time()
prediction = requests.post(f"{BASE_URL}/predict", json=payload)
elapsed_ms = (time.time() - start) * 1000
print("predict:", prediction.status_code, f"{elapsed_ms:.2f}ms")

metrics = requests.get(f"{BASE_URL}/metrics")
print("metrics:", metrics.status_code)
```

## Docker Check

```bash
docker build -t farming-ai-model:test .
docker run -p 8000:8000 --name ai-test farming-ai-model:test
curl http://localhost:8000/health
docker stop ai-test
docker rm ai-test
```

## What To Verify

- `/health` responds successfully
- `/predict` accepts the current sample payload
- `/metrics` exposes Prometheus-style output
- `/resources/check` reports local runtime information

## Troubleshooting

- If the service does not start, run `python agri_predictor.py` directly to inspect logs.
- If model artifacts are missing, rerun `python trainer.py`.
- If prediction latency is high, inspect `/resources/check` and `/metrics`.
