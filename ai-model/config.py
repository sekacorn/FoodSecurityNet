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
