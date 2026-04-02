from fastapi import FastAPI, HTTPException, status
from fastapi.responses import JSONResponse, PlainTextResponse
from pydantic import BaseModel, Field
from typing import List, Dict, Any
import torch
import numpy as np
import pandas as pd
import psutil
import os
from datetime import datetime
from prometheus_client import Counter, Histogram, Gauge, generate_latest, CONTENT_TYPE_LATEST
import multiprocessing as mp

from model import FarmingRecommendationModel
from data_processor import FarmingDataProcessor
import config


# Prometheus metrics
PREDICTION_COUNTER = Counter('predictions_total', 'Total number of predictions made')
PREDICTION_DURATION = Histogram('prediction_duration_seconds', 'Time spent making predictions')
MODEL_LOAD_TIME = Gauge('model_load_time_seconds', 'Time taken to load the model')
ACTIVE_REQUESTS = Gauge('active_requests', 'Number of active requests')
CPU_USAGE = Gauge('cpu_usage_percent', 'CPU usage percentage')
MEMORY_USAGE = Gauge('memory_usage_percent', 'Memory usage percentage')


# Pydantic models for request/response
class AgriculturalData(BaseModel):
    """Agricultural input data"""
    farm_size: float = Field(..., gt=0, description="Farm size in hectares")
    soil_ph: float = Field(..., ge=0, le=14, description="Soil pH level")
    soil_nitrogen: float = Field(..., ge=0, le=100, description="Soil nitrogen content (%)")
    soil_phosphorus: float = Field(..., ge=0, le=100, description="Soil phosphorus content (%)")
    soil_potassium: float = Field(..., ge=0, le=100, description="Soil potassium content (%)")
    irrigation_frequency: int = Field(..., ge=0, le=7, description="Irrigation frequency per week")
    soil_type: str = Field(..., description="Type of soil")
    current_crop: str = Field(..., description="Currently planted crop")


class EnvironmentalData(BaseModel):
    """Environmental input data"""
    rainfall: float = Field(..., ge=0, description="Annual rainfall in mm")
    temperature: float = Field(..., description="Average temperature in Celsius")
    humidity: float = Field(..., ge=0, le=100, description="Average humidity (%)")
    elevation: float = Field(..., ge=0, description="Elevation in meters")
    region: str = Field(..., description="Geographic region")


class SocioeconomicData(BaseModel):
    """Socioeconomic input data"""
    income: float = Field(..., ge=0, description="Annual income")
    distance_to_market: float = Field(..., ge=0, description="Distance to nearest market in km")
    education_level: int = Field(..., ge=0, le=4, description="Education level (0-4)")
    access_to_credit: bool = Field(..., description="Access to agricultural credit")
    extension_services: bool = Field(..., description="Access to extension services")


class PredictionRequest(BaseModel):
    """Complete prediction request"""
    agricultural: AgriculturalData
    environmental: EnvironmentalData
    socioeconomic: SocioeconomicData


class Recommendation(BaseModel):
    """Single recommendation"""
    strategy: str
    confidence: float
    reasoning: str


class PredictionResponse(BaseModel):
    """Prediction response"""
    crop_recommendations: List[Recommendation]
    irrigation_recommendations: List[Recommendation]
    fertilization_recommendations: List[Recommendation]
    overall_strategy: str
    confidence_score: float
    timestamp: str


class HealthResponse(BaseModel):
    """Health check response"""
    status: str
    model_loaded: bool
    version: str
    timestamp: str


class ResourceInfo(BaseModel):
    """System resource information"""
    cpu_cores: int
    cpu_percent: float
    memory_total_gb: float
    memory_available_gb: float
    memory_percent: float
    gpu_available: bool
    multiprocessing_enabled: bool


# FastAPI app
app = FastAPI(
    title="Agricultural Prediction Service",
    description="AI-powered farming recommendations using PyTorch",
    version=config.MODEL_VERSION
)


class ModelService:
    """Singleton service for model management"""

    _instance = None
    _model = None
    _processor = None
    _device = None
    _multiprocessing_enabled = False

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(ModelService, cls).__new__(cls)
        return cls._instance

    def initialize(self):
        """Load model and processor"""
        if self._model is not None:
            return

        print("Initializing model service...")
        start_time = datetime.now()

        # Check for GPU
        self._device = 'cuda' if torch.cuda.is_available() else 'cpu'
        print(f"Using device: {self._device}")

        # Check system resources for multiprocessing
        cpu_cores = mp.cpu_count()
        memory_gb = psutil.virtual_memory().total / (1024 ** 3)

        if cpu_cores >= config.MIN_CPU_CORES_FOR_MULTIPROCESSING and \
           memory_gb >= config.MIN_MEMORY_GB_FOR_MULTIPROCESSING:
            self._multiprocessing_enabled = True
            print(f"Multiprocessing enabled (CPU: {cpu_cores} cores, RAM: {memory_gb:.1f} GB)")
        else:
            print(f"Multiprocessing disabled (CPU: {cpu_cores} cores, RAM: {memory_gb:.1f} GB)")

        # Load processor
        processor_path = 'data_processor.pkl'
        if os.path.exists(processor_path):
            print(f"Loading data processor from {processor_path}")
            self._processor = FarmingDataProcessor.load(processor_path)
        else:
            print("Warning: Data processor not found. Creating new processor.")
            self._processor = FarmingDataProcessor()

        # Load or create model
        model_path = config.MODEL_PATH
        if os.path.exists(model_path):
            print(f"Loading model from {model_path}")

            # Get input size from processor
            input_size = len(self._processor.feature_names) if self._processor.is_fitted else config.INPUT_SIZE

            self._model = FarmingRecommendationModel(
                input_size=input_size,
                hidden_sizes=config.HIDDEN_SIZES,
                output_size=config.OUTPUT_SIZE,
                dropout_rate=config.DROPOUT_RATE
            )

            checkpoint = torch.load(model_path, map_location=self._device)
            self._model.load_state_dict(checkpoint['model_state_dict'])
            self._model.to(self._device)
            self._model.eval()

            print("Model loaded successfully")
        else:
            print(f"Warning: Model file not found at {model_path}. Creating untrained model.")
            self._model = FarmingRecommendationModel(
                input_size=config.INPUT_SIZE,
                hidden_sizes=config.HIDDEN_SIZES,
                output_size=config.OUTPUT_SIZE,
                dropout_rate=config.DROPOUT_RATE
            )
            self._model.to(self._device)
            self._model.eval()

        load_time = (datetime.now() - start_time).total_seconds()
        MODEL_LOAD_TIME.set(load_time)
        print(f"Model service initialized in {load_time:.2f} seconds")

    def preprocess_request(self, request: PredictionRequest) -> pd.DataFrame:
        """Convert request to dataframe"""
        data = {
            # Agricultural
            'farm_size': request.agricultural.farm_size,
            'soil_ph': request.agricultural.soil_ph,
            'soil_nitrogen': request.agricultural.soil_nitrogen,
            'soil_phosphorus': request.agricultural.soil_phosphorus,
            'soil_potassium': request.agricultural.soil_potassium,
            'irrigation_frequency': request.agricultural.irrigation_frequency,
            'soil_type': request.agricultural.soil_type,
            'current_crop': request.agricultural.current_crop,

            # Environmental
            'rainfall': request.environmental.rainfall,
            'temperature': request.environmental.temperature,
            'humidity': request.environmental.humidity,
            'elevation': request.environmental.elevation,
            'region': request.environmental.region,

            # Socioeconomic
            'income': request.socioeconomic.income,
            'distance_to_market': request.socioeconomic.distance_to_market,
            'education_level': request.socioeconomic.education_level,
            'access_to_credit': int(request.socioeconomic.access_to_credit),
            'extension_services': int(request.socioeconomic.extension_services)
        }

        return pd.DataFrame([data])

    def predict(self, request: PredictionRequest) -> PredictionResponse:
        """Make prediction"""
        ACTIVE_REQUESTS.inc()
        PREDICTION_COUNTER.inc()

        try:
            with PREDICTION_DURATION.time():
                # Preprocess
                df = self.preprocess_request(request)

                # Transform data
                if self._processor.is_fitted:
                    X = self._processor.transform(df)
                else:
                    # If processor not fitted, use raw values (not recommended for production)
                    X = df.select_dtypes(include=[np.number]).values

                # Convert to tensor
                X_tensor = torch.FloatTensor(X).to(self._device)

                # Predict
                with torch.no_grad():
                    predictions = self._model.predict_probabilities(X_tensor)
                    predictions_np = predictions.cpu().numpy()[0]

                # Generate recommendations
                top_k = 3
                top_indices = np.argsort(predictions_np)[-top_k:][::-1]

                # Map predictions to strategies
                crop_recs = [
                    Recommendation(
                        strategy=config.CROP_TYPES[i % len(config.CROP_TYPES)],
                        confidence=float(predictions_np[i]),
                        reasoning=f"Based on soil conditions and climate patterns"
                    )
                    for i in top_indices[:len(config.CROP_TYPES)]
                ][:top_k]

                irrigation_recs = [
                    Recommendation(
                        strategy=config.IRRIGATION_METHODS[i % len(config.IRRIGATION_METHODS)],
                        confidence=float(predictions_np[i]),
                        reasoning=f"Optimized for water efficiency and crop needs"
                    )
                    for i in top_indices[:len(config.IRRIGATION_METHODS)]
                ][:top_k]

                fertilization_recs = [
                    Recommendation(
                        strategy=config.FERTILIZATION_STRATEGIES[i % len(config.FERTILIZATION_STRATEGIES)],
                        confidence=float(predictions_np[i]),
                        reasoning=f"Tailored to soil nutrient levels"
                    )
                    for i in top_indices[:len(config.FERTILIZATION_STRATEGIES)]
                ][:top_k]

                # Overall strategy
                overall_strategy = f"Recommended: {crop_recs[0].strategy} with {irrigation_recs[0].strategy} irrigation"

                return PredictionResponse(
                    crop_recommendations=crop_recs,
                    irrigation_recommendations=irrigation_recs,
                    fertilization_recommendations=fertilization_recs,
                    overall_strategy=overall_strategy,
                    confidence_score=float(predictions_np[top_indices[0]]),
                    timestamp=datetime.now().isoformat()
                )

        finally:
            ACTIVE_REQUESTS.dec()


# Initialize service
model_service = ModelService()


@app.on_event("startup")
async def startup_event():
    """Initialize model on startup"""
    model_service.initialize()
    print("Service ready to accept requests")


@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """
    Make farming predictions

    Takes agricultural, environmental, and socioeconomic data
    Returns recommendations for crop selection, irrigation, and fertilization
    """
    try:
        return model_service.predict(request)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Prediction error: {str(e)}"
        )


@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint"""
    return HealthResponse(
        status="healthy",
        model_loaded=model_service._model is not None,
        version=config.MODEL_VERSION,
        timestamp=datetime.now().isoformat()
    )


@app.get("/metrics")
async def metrics():
    """Prometheus metrics endpoint"""
    # Update system metrics
    CPU_USAGE.set(psutil.cpu_percent())
    MEMORY_USAGE.set(psutil.virtual_memory().percent)

    return PlainTextResponse(
        generate_latest(),
        media_type=CONTENT_TYPE_LATEST
    )


@app.get("/resources/check", response_model=ResourceInfo)
async def check_resources():
    """Check system resources"""
    memory = psutil.virtual_memory()

    return ResourceInfo(
        cpu_cores=mp.cpu_count(),
        cpu_percent=psutil.cpu_percent(interval=1),
        memory_total_gb=memory.total / (1024 ** 3),
        memory_available_gb=memory.available / (1024 ** 3),
        memory_percent=memory.percent,
        gpu_available=torch.cuda.is_available(),
        multiprocessing_enabled=model_service._multiprocessing_enabled
    )


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "Agricultural Prediction Service",
        "version": config.MODEL_VERSION,
        "status": "running",
        "endpoints": {
            "predict": "/predict",
            "health": "/health",
            "metrics": "/metrics",
            "resources": "/resources/check",
            "docs": "/docs"
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "agri_predictor:app",
        host=config.HOST,
        port=config.PORT,
        workers=config.WORKERS,
        log_level=config.LOG_LEVEL
    )
