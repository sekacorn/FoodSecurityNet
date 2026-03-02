# Multi-stage Docker build for AI Model Service

# Stage 1: Builder
FROM python:3.10-slim as builder

# Set working directory
WORKDIR /app

# Install build dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir --user -r requirements.txt


# Stage 2: Runtime
FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# Copy Python packages from builder
COPY --from=builder /root/.local /root/.local

# Make sure scripts in .local are usable
ENV PATH=/root/.local/bin:$PATH

# Copy application files
COPY agri_predictor.py .
COPY model.py .
COPY data_processor.py .
COPY config.py .
COPY trainer.py .

# Create directory for model files
RUN mkdir -p /app/models

# Copy model files if they exist (optional)
# COPY model.pt .
# COPY data_processor.pkl .

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV MODEL_PATH=model.pt
ENV HOST=0.0.0.0
ENV PORT=8000
ENV WORKERS=1
ENV LOG_LEVEL=info

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/health')" || exit 1

# Run the application
CMD ["python", "-m", "uvicorn", "agri_predictor:app", "--host", "0.0.0.0", "--port", "8000"]
