"""
LLM Service - FastAPI service for natural language query processing
"""
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
import logging
import uvicorn

from model_manager import ModelManager
from query_processor import QueryProcessor
from config import SERVICE_HOST, SERVICE_PORT, LOG_LEVEL

# Configure logging
logging.basicConfig(
    level=getattr(logging, LOG_LEVEL),
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize FastAPI app
app = FastAPI(
    title="FoodSecurityNet LLM Service",
    description="Natural language processing service for agricultural queries",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize components
model_manager = ModelManager()
query_processor = QueryProcessor()


# Pydantic models
class QueryRequest(BaseModel):
    query: str = Field(..., description="Natural language query")
    user_id: Optional[str] = Field(None, description="User identifier")
    context: Optional[Dict[str, Any]] = Field(default_factory=dict, description="Additional context")
    model: Optional[str] = Field(None, description="Specific model to use")

    class Config:
        json_schema_extra = {
            "example": {
                "query": "What's the best time to plant maize in Kenya?",
                "user_id": "user123",
                "context": {
                    "location": "Nairobi, Kenya",
                    "farm_size": "2 hectares"
                }
            }
        }


class TroubleshootRequest(BaseModel):
    problem: str = Field(..., description="Description of the problem")
    crop_type: Optional[str] = Field(None, description="Type of crop")
    symptoms: Optional[List[str]] = Field(default_factory=list, description="Observed symptoms")
    user_id: Optional[str] = Field(None, description="User identifier")
    context: Optional[Dict[str, Any]] = Field(default_factory=dict, description="Additional context")

    class Config:
        json_schema_extra = {
            "example": {
                "problem": "My tomato plants have yellow leaves and are wilting",
                "crop_type": "tomato",
                "symptoms": ["yellow leaves", "wilting"],
                "context": {
                    "location": "Mombasa, Kenya"
                }
            }
        }


class QueryResponse(BaseModel):
    response: str
    intent: str
    query_type: str
    entities: Dict[str, List[str]]
    metadata: Dict[str, Any]


class HealthResponse(BaseModel):
    status: str
    models_loaded: List[str]
    device: str


# API Endpoints
@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    """
    Health check endpoint
    """
    return HealthResponse(
        status="healthy",
        models_loaded=model_manager.get_loaded_models(),
        device=model_manager.device
    )


@app.post("/api/query", response_model=QueryResponse, tags=["Query"])
async def process_query(request: QueryRequest):
    """
    Process a natural language query
    """
    try:
        logger.info(f"Processing query: {request.query}")

        # Process query to extract intent and context
        query_context = query_processor.process_query(
            request.query,
            user_context=request.context
        )

        # Enhance prompt with context
        enhanced_prompt = query_processor.enhance_prompt(
            request.query,
            query_context
        )

        # Generate response using model manager
        raw_response = model_manager.generate_response(
            enhanced_prompt,
            model_name=request.model
        )

        # Format response
        formatted = query_processor.format_response(raw_response, query_context)

        return QueryResponse(
            response=formatted['response'],
            intent=formatted['intent'],
            query_type=formatted['query_type'],
            entities=formatted['entities'],
            metadata=formatted['metadata']
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error processing query: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error processing query: {str(e)}"
        )


@app.post("/api/troubleshoot", response_model=QueryResponse, tags=["Troubleshooting"])
async def troubleshoot_problem(request: TroubleshootRequest):
    """
    Troubleshoot agricultural problems with diagnostic analysis
    """
    try:
        logger.info(f"Troubleshooting problem: {request.problem}")

        # Build troubleshooting query
        query_parts = [request.problem]

        if request.crop_type:
            query_parts.append(f"Crop: {request.crop_type}")

        if request.symptoms:
            query_parts.append(f"Symptoms: {', '.join(request.symptoms)}")

        troubleshoot_query = ". ".join(query_parts)

        # Process as troubleshooting query
        query_context = query_processor.process_query(
            troubleshoot_query,
            user_context=request.context
        )

        # Override intent to troubleshooting
        query_context['intent'] = 'troubleshooting'

        # Build diagnostic prompt
        diagnostic_prompt = f"""
You are an expert agricultural diagnostician. Analyze the following problem and provide:
1. Likely causes
2. Recommended solutions
3. Preventive measures
4. When to seek additional help

Problem: {request.problem}
"""

        if request.crop_type:
            diagnostic_prompt += f"\nCrop: {request.crop_type}"

        if request.symptoms:
            diagnostic_prompt += f"\nSymptoms: {', '.join(request.symptoms)}"

        if request.context:
            diagnostic_prompt += f"\nContext: {request.context}"

        diagnostic_prompt += "\n\nProvide your diagnostic analysis:"

        # Generate diagnostic response
        raw_response = model_manager.generate_response(diagnostic_prompt)

        # Format response
        formatted = query_processor.format_response(raw_response, query_context)

        return QueryResponse(
            response=formatted['response'],
            intent='troubleshooting',
            query_type='diagnostic',
            entities=query_context.get('entities', {}),
            metadata={
                'type': 'diagnostic',
                'crop_type': request.crop_type,
                'symptoms': request.symptoms
            }
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in troubleshooting: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error in troubleshooting: {str(e)}"
        )


@app.get("/api/models", tags=["Models"])
async def get_models():
    """
    Get information about available and loaded models
    """
    return {
        "loaded_models": model_manager.get_loaded_models(),
        "available_models": list(model_manager.model_configs.keys()),
        "device": model_manager.device,
        "using_api": model_manager.use_xai
    }


@app.post("/api/models/load", tags=["Models"])
async def load_model(model_name: str):
    """
    Load a specific model into memory
    """
    try:
        success = model_manager.load_model(model_name)
        if success:
            return {
                "status": "success",
                "message": f"Model {model_name} loaded successfully",
                "loaded_models": model_manager.get_loaded_models()
            }
        else:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Failed to load model {model_name}"
            )
    except Exception as e:
        logger.error(f"Error loading model: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error loading model: {str(e)}"
        )


# Startup event
@app.on_event("startup")
async def startup_event():
    """
    Initialize models on startup
    """
    logger.info("Starting LLM Service...")
    logger.info(f"Device: {model_manager.device}")

    # Load default model
    try:
        logger.info("Loading default model...")
        model_manager.load_model()
        logger.info("Default model loaded successfully")
    except Exception as e:
        logger.error(f"Error loading default model: {str(e)}")


# Shutdown event
@app.on_event("shutdown")
async def shutdown_event():
    """
    Cleanup on shutdown
    """
    logger.info("Shutting down LLM Service...")


if __name__ == "__main__":
    uvicorn.run(
        "llm_service:app",
        host=SERVICE_HOST,
        port=SERVICE_PORT,
        reload=True,
        log_level=LOG_LEVEL.lower()
    )
