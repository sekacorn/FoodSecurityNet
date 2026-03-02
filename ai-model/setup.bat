@echo off
REM Setup script for AI Model Service (Windows)

echo ================================================
echo AI Model Service Setup
echo ================================================

REM Create virtual environment
echo.
echo [1/5] Creating virtual environment...
python -m venv venv
call venv\Scripts\activate.bat

REM Install dependencies
echo.
echo [2/5] Installing dependencies...
python -m pip install --upgrade pip
pip install -r requirements.txt

REM Copy environment file
echo.
echo [3/5] Setting up configuration...
if not exist .env (
    copy .env.example .env
    echo Created .env file. Please update with your settings.
) else (
    echo .env file already exists.
)

REM Train model
echo.
echo [4/5] Training model...
set /p train_choice="Do you want to train a new model now? (y/n): "
if /i "%train_choice%"=="y" (
    python trainer.py
    echo Model training complete!
) else (
    echo Skipping model training. You can run 'python trainer.py' later.
)

REM Setup complete
echo.
echo [5/5] Setup complete!
echo.
echo ================================================
echo Next Steps:
echo ================================================
echo 1. Activate virtual environment: venv\Scripts\activate.bat
echo 2. Update .env file with your configuration
echo 3. Train model (if skipped): python trainer.py
echo 4. Start service: python agri_predictor.py
echo 5. Access API docs: http://localhost:8000/docs
echo 6. Test prediction: curl -X POST http://localhost:8000/predict -H "Content-Type: application/json" -d @sample_request.json
echo.
echo For Docker deployment:
echo   docker build -t farming-ai-model .
echo   docker run -p 8000:8000 farming-ai-model
echo ================================================

pause
