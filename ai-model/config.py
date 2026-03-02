from decouple import config

# Model Configuration
MODEL_PATH = config('MODEL_PATH', default='model.pt')
MODEL_VERSION = config('MODEL_VERSION', default='1.0.0')

# Server Configuration
HOST = config('HOST', default='0.0.0.0')
PORT = config('PORT', default=8000, cast=int)
WORKERS = config('WORKERS', default=1, cast=int)
LOG_LEVEL = config('LOG_LEVEL', default='info')

# Resource Thresholds
MIN_CPU_CORES_FOR_MULTIPROCESSING = config('MIN_CPU_CORES_FOR_MULTIPROCESSING', default=4, cast=int)
MIN_MEMORY_GB_FOR_MULTIPROCESSING = config('MIN_MEMORY_GB_FOR_MULTIPROCESSING', default=8, cast=int)

# Model Hyperparameters
INPUT_SIZE = config('INPUT_SIZE', default=50, cast=int)
HIDDEN_SIZES = [256, 128, 64, 32]
OUTPUT_SIZE = config('OUTPUT_SIZE', default=15, cast=int)
DROPOUT_RATE = config('DROPOUT_RATE', default=0.3, cast=float)

# Training Configuration
BATCH_SIZE = config('BATCH_SIZE', default=32, cast=int)
LEARNING_RATE = config('LEARNING_RATE', default=0.001, cast=float)
EPOCHS = config('EPOCHS', default=100, cast=int)
VALIDATION_SPLIT = config('VALIDATION_SPLIT', default=0.2, cast=float)

# Feature Configuration
CROP_TYPES = ['maize', 'wheat', 'rice', 'sorghum', 'millet', 'cassava', 'beans', 'vegetables']
IRRIGATION_METHODS = ['drip', 'sprinkler', 'flood', 'furrow', 'rainfed']
FERTILIZATION_STRATEGIES = ['organic', 'synthetic', 'mixed', 'precision', 'traditional']
MBTI_TYPES = ['INTJ', 'INTP', 'ENTJ', 'ENTP', 'INFJ', 'INFP', 'ENFJ', 'ENFP',
              'ISTJ', 'ISFJ', 'ESTJ', 'ESFJ', 'ISTP', 'ISFP', 'ESTP', 'ESFP']

# MBTI Recommendation Modifiers
MBTI_PREFERENCES = {
    'INTJ': {'innovation_weight': 1.3, 'efficiency_focus': 1.2, 'data_driven': 1.5},
    'INTP': {'innovation_weight': 1.4, 'efficiency_focus': 1.1, 'data_driven': 1.4},
    'ENTJ': {'innovation_weight': 1.2, 'efficiency_focus': 1.4, 'data_driven': 1.3},
    'ENTP': {'innovation_weight': 1.5, 'efficiency_focus': 1.0, 'data_driven': 1.2},
    'INFJ': {'innovation_weight': 1.1, 'efficiency_focus': 1.0, 'data_driven': 1.1, 'sustainability_focus': 1.4},
    'INFP': {'innovation_weight': 1.0, 'efficiency_focus': 0.9, 'data_driven': 1.0, 'sustainability_focus': 1.5},
    'ENFJ': {'innovation_weight': 1.1, 'efficiency_focus': 1.1, 'data_driven': 1.0, 'sustainability_focus': 1.3},
    'ENFP': {'innovation_weight': 1.2, 'efficiency_focus': 0.9, 'data_driven': 1.0, 'sustainability_focus': 1.2},
    'ISTJ': {'innovation_weight': 0.8, 'efficiency_focus': 1.3, 'data_driven': 1.2, 'traditional_preference': 1.3},
    'ISFJ': {'innovation_weight': 0.7, 'efficiency_focus': 1.1, 'data_driven': 1.0, 'traditional_preference': 1.4},
    'ESTJ': {'innovation_weight': 0.9, 'efficiency_focus': 1.5, 'data_driven': 1.3, 'traditional_preference': 1.2},
    'ESFJ': {'innovation_weight': 0.8, 'efficiency_focus': 1.2, 'data_driven': 1.1, 'traditional_preference': 1.2},
    'ISTP': {'innovation_weight': 1.2, 'efficiency_focus': 1.2, 'data_driven': 1.3, 'hands_on': 1.4},
    'ISFP': {'innovation_weight': 1.0, 'efficiency_focus': 0.9, 'data_driven': 0.9, 'sustainability_focus': 1.3},
    'ESTP': {'innovation_weight': 1.1, 'efficiency_focus': 1.3, 'data_driven': 1.2, 'hands_on': 1.3},
    'ESFP': {'innovation_weight': 1.0, 'efficiency_focus': 1.0, 'data_driven': 1.0, 'sustainability_focus': 1.1}
}
