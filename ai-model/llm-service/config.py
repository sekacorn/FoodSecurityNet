"""
Configuration module for LLM service
"""
from decouple import config
import os

# API Configuration
XAI_API_KEY = config('XAI_API_KEY', default='')
XAI_API_URL = config('XAI_API_URL', default='https://api.x.ai/v1')
OPENAI_API_KEY = config('OPENAI_API_KEY', default='')

# Model Configuration
DEFAULT_MODEL = config('DEFAULT_MODEL', default='gpt2')
FALLBACK_MODEL = config('FALLBACK_MODEL', default='flan-t5-base')
USE_XAI = config('USE_XAI', default='false', cast=bool)

# Service Configuration
SERVICE_PORT = config('SERVICE_PORT', default=8001, cast=int)
SERVICE_HOST = config('SERVICE_HOST', default='0.0.0.0')
MAX_LENGTH = config('MAX_LENGTH', default=512, cast=int)
TEMPERATURE = config('TEMPERATURE', default=0.7, cast=float)

# Model Cache
MODEL_CACHE_DIR = config('MODEL_CACHE_DIR', default='./model_cache')
os.makedirs(MODEL_CACHE_DIR, exist_ok=True)

# MBTI Configuration
MBTI_TYPES = [
    'ENTJ', 'INTJ', 'ENTP', 'INTP',
    'ENFJ', 'INFJ', 'ENFP', 'INFP',
    'ESTJ', 'ISTJ', 'ESFJ', 'ISFJ',
    'ESTP', 'ISTP', 'ESFP', 'ISFP'
]

# Logging
LOG_LEVEL = config('LOG_LEVEL', default='INFO')
