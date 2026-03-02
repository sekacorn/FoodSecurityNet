@echo off
echo ============================================
echo FoodSecurityNet End-to-End Test Runner
echo ============================================
echo.

echo [1/4] Checking if services are running...
curl -s http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    echo ERROR: API Gateway is not running!
    echo Please start services with: docker-compose up -d
    exit /b 1
)
echo   ✓ API Gateway is running

curl -s http://localhost:8000/health >nul 2>&1
if errorlevel 1 (
    echo WARNING: AI Model service is not running
    echo   Some tests may be skipped
) else (
    echo   ✓ AI Model service is running
)

curl -s http://localhost:8001/health >nul 2>&1
if errorlevel 1 (
    echo WARNING: LLM service is not running
    echo   Some tests may be skipped
) else (
    echo   ✓ LLM service is running
)

echo.
echo [2/4] Installing test dependencies...
cd tests\e2e
pip install -q -r requirements.txt
if errorlevel 1 (
    echo ERROR: Failed to install dependencies
    exit /b 1
)
echo   ✓ Dependencies installed

echo.
echo [3/4] Running E2E tests...
echo.
pytest -v --html=test-report.html --self-contained-html

set TEST_EXIT_CODE=%errorlevel%

echo.
echo [4/4] Test Summary
echo ============================================
if %TEST_EXIT_CODE%==0 (
    echo   ✓ All tests passed!
    echo   Test report: tests\e2e\test-report.html
) else (
    echo   ✗ Some tests failed
    echo   Check test-report.html for details
)
echo ============================================

exit /b %TEST_EXIT_CODE%
