#!/bin/bash

# Setup script for AI Model Service

echo "================================================"
echo "AI Model Service Setup"
echo "================================================"

# Create virtual environment
echo -e "\n[1/5] Creating virtual environment..."
python3 -m venv venv
source venv/bin/activate

# Install dependencies
echo -e "\n[2/5] Installing dependencies..."
pip install --upgrade pip
pip install -r requirements.txt

# Copy environment file
echo -e "\n[3/5] Setting up configuration..."
if [ ! -f .env ]; then
    cp .env.example .env
    echo "Created .env file. Please update with your settings."
else
    echo ".env file already exists."
fi

# Train model
echo -e "\n[4/5] Training model (this may take a few minutes)..."
read -p "Do you want to train a new model now? (y/n): " train_choice
if [ "$train_choice" = "y" ]; then
    python trainer.py
    echo "Model training complete!"
else
    echo "Skipping model training. You can run 'python trainer.py' later."
fi

# Test service
echo -e "\n[5/5] Setup complete!"
echo ""
echo "================================================"
echo "Next Steps:"
echo "================================================"
echo "1. Activate virtual environment: source venv/bin/activate"
echo "2. Update .env file with your configuration"
echo "3. Train model (if skipped): python trainer.py"
echo "4. Start service: python agri_predictor.py"
echo "5. Access API docs: http://localhost:8000/docs"
echo "6. Test prediction: curl -X POST http://localhost:8000/predict -H 'Content-Type: application/json' -d @sample_request.json"
echo ""
echo "For Docker deployment:"
echo "  docker build -t farming-ai-model ."
echo "  docker run -p 8000:8000 farming-ai-model"
echo "================================================"
