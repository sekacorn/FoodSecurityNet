# AI Model Service

Standalone FastAPI and PyTorch service for agricultural prediction workloads inside FoodSecurityNet.

This service usually runs on port `8000` for internal service-to-service traffic. The browser-facing web app still talks to the API gateway on port `8080`.

For the shortest setup path, use [QUICKSTART.md](QUICKSTART.md). For validation and load checks, use [TESTING.md](TESTING.md).

## What It Does

- serves prediction requests through FastAPI
- exposes health, metrics, and resource-check endpoints
- loads trained model artifacts from local files
- supports containerized deployment

## Key Endpoints

- `POST /predict`
- `GET /health`
- `GET /metrics`
- `GET /resources/check`

## Local Run

```bash
pip install -r requirements.txt
python trainer.py
python agri_predictor.py
```

Alternative:

```bash
uvicorn agri_predictor:app --host 0.0.0.0 --port 8000
```

## Sample Request Shape

See [sample_request.json](sample_request.json) for the current example payload. The request includes:

- `agricultural`
- `environmental`
- `socioeconomic`

## Configuration

Typical local environment values:

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

## Files

```text
ai-model/
|-- agri_predictor.py
|-- config.py
|-- data_processor.py
|-- model.py
|-- trainer.py
|-- sample_request.json
|-- QUICKSTART.md
|-- TESTING.md
`-- README.md
```

## Notes

- This service is part of the larger FoodSecurityNet platform but is not the direct browser entry point.
- Frontend and gateway documentation live in the repository root and `frontend/` and `backend/api-gateway/`.

## License

Apache License 2.0. See the repository [LICENSE](../LICENSE).
