# FoodSecurityNet End-to-End Testing Guide

## Overview

This guide explains how to run comprehensive end-to-end tests for the FoodSecurityNet application. The tests cover the complete user journey from registration to data visualization and AI predictions.

## Prerequisites

1. **Docker and Docker Compose** installed
2. **Python 3.10+** installed
3. **All services running** via docker-compose

## Test Coverage

### 1. Complete User Workflow (test_complete_workflow.py)

#### Authentication Tests
- ✅ User registration with MBTI type
- ✅ User login with JWT tokens
- ✅ Get current user information
- ✅ JWT token refresh
- ✅ User logout

#### Data Management Tests
- ✅ Upload agricultural data (JSON format)
- ✅ Upload environmental data
- ✅ Upload socioeconomic data
- ✅ Retrieve uploaded data
- ✅ Data validation and error handling

#### Visualization Tests
- ✅ Create 3D visualizations
- ✅ Retrieve visualizations
- ✅ Export visualizations (PNG, SVG, STL)

#### AI & ML Tests
- ✅ AI farming predictions with PyTorch model
- ✅ Crop selection recommendations
- ✅ Irrigation method suggestions
- ✅ Fertilization strategy recommendations
- ✅ MBTI-tailored recommendations

#### LLM Integration Tests
- ✅ Natural language queries
- ✅ Context-aware responses
- ✅ MBTI personalization (all 16 types)
- ✅ Troubleshooting queries

#### Annotation Tests
- ✅ Create annotations on visualizations
- ✅ Retrieve annotations
- ✅ Update annotations

#### Resource Monitoring Tests
- ✅ CPU, memory, GPU availability checks
- ✅ System resource reporting

### 2. Data Validation Tests

- ✅ Invalid registration data rejection
- ✅ Invalid agricultural data rejection
- ✅ Invalid AI prediction data rejection
- ✅ Proper error messages for validation failures

### 3. MBTI Personalization Tests

Tests all 16 MBTI personality types:

**Analysts (NT)**
- ✅ INTJ (Mastermind)
- ✅ INTP (Thinker)
- ✅ ENTJ (Commander)
- ✅ ENTP (Debater)

**Diplomats (NF)**
- ✅ INFJ (Advocate)
- ✅ INFP (Mediator)
- ✅ ENFJ (Protagonist)
- ✅ ENFP (Campaigner)

**Sentinels (SJ)**
- ✅ ISTJ (Logistician)
- ✅ ISFJ (Defender)
- ✅ ESTJ (Executive)
- ✅ ESFJ (Consul)

**Explorers (SP)**
- ✅ ISTP (Virtuoso)
- ✅ ISFP (Adventurer)
- ✅ ESTP (Entrepreneur)
- ✅ ESFP (Entertainer)

### 4. System Health Tests

- ✅ API Gateway health check
- ✅ AI Model service health check
- ✅ LLM service health check
- ✅ Database connectivity
- ✅ Redis connectivity

## Running the Tests

### Option 1: Quick Test (Windows)

```bash
# Navigate to project root
cd "C:\Users\sekac\OneDrive\Desktop\My Beautiful Ideas\Idea For FoodSecurityNet\FoodSecurityNet"

# Start all services
docker-compose up -d

# Wait for services to be ready (about 2-3 minutes)
timeout /t 180

# Run tests
tests\run_e2e_tests.bat
```

### Option 2: Manual Test Execution

```bash
# 1. Start all services
docker-compose up -d

# 2. Verify services are running
curl http://localhost:8080/actuator/health
curl http://localhost:8000/health
curl http://localhost:8001/health
curl http://localhost:3000

# 3. Install test dependencies
cd tests/e2e
pip install -r requirements.txt

# 4. Run tests
pytest -v --html=test-report.html --self-contained-html

# 5. View results
# Open test-report.html in your browser
```

### Option 3: Run Specific Test Classes

```bash
# Run only authentication tests
pytest test_complete_workflow.py::TestCompleteUserWorkflow::test_01_user_registration -v

# Run only AI tests
pytest test_complete_workflow.py::TestCompleteUserWorkflow::test_07_ai_prediction -v

# Run only MBTI personalization tests
pytest test_complete_workflow.py::TestMBTIPersonalization -v

# Run only health checks
pytest test_complete_workflow.py::TestSystemHealth -v
```

### Option 4: Run Tests in Parallel

```bash
# Run tests in parallel using 4 workers
pytest -n 4 -v
```

## Test Output

### Console Output Example

```
============================================
FoodSecurityNet End-to-End Test Runner
============================================

[1/4] Checking if services are running...
  ✓ API Gateway is running
  ✓ AI Model service is running
  ✓ LLM service is running

[2/4] Installing test dependencies...
  ✓ Dependencies installed

[3/4] Running E2E tests...

test_complete_workflow.py::TestCompleteUserWorkflow::test_01_user_registration PASSED
✓ User registered successfully: testuser_1234567890

test_complete_workflow.py::TestCompleteUserWorkflow::test_02_user_login PASSED
✓ User logged in successfully
  Access Token: eyJhbGciOiJIUzI1NiIs...

test_complete_workflow.py::TestCompleteUserWorkflow::test_03_get_current_user PASSED
✓ Current user retrieved: testuser_1234567890

test_complete_workflow.py::TestCompleteUserWorkflow::test_04_upload_agricultural_data PASSED
✓ Agricultural data uploaded successfully

test_complete_workflow.py::TestCompleteUserWorkflow::test_07_ai_prediction PASSED
✓ AI prediction successful
  Top crop: maize
  Top irrigation: drip

test_complete_workflow.py::TestCompleteUserWorkflow::test_08_llm_query PASSED
✓ LLM query successful
  Query: What crops should I plant this season in Kenya?
  Response: Based on your location in Kenya and the current season, I recommend planting maize...

[4/4] Test Summary
============================================
  ✓ All tests passed!
  Test report: tests\e2e\test-report.html
============================================
```

### HTML Report

The test run generates an HTML report (`test-report.html`) with:
- ✅ Total tests run
- ✅ Pass/fail statistics
- ✅ Execution time for each test
- ✅ Detailed error messages for failures
- ✅ Screenshot on failure (if configured)

## Expected Test Results

When all services are running correctly, you should see:

```
======================== test session starts ========================
collected 25 items

test_complete_workflow.py::TestCompleteUserWorkflow::test_01_user_registration PASSED [4%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_02_user_login PASSED [8%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_03_get_current_user PASSED [12%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_04_upload_agricultural_data PASSED [16%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_05_get_agricultural_data PASSED [20%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_06_create_visualization PASSED [24%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_07_ai_prediction PASSED [28%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_08_llm_query PASSED [32%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_09_resource_monitoring PASSED [36%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_10_create_annotation PASSED [40%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_11_token_refresh PASSED [44%]
test_complete_workflow.py::TestCompleteUserWorkflow::test_12_logout PASSED [48%]
test_complete_workflow.py::TestDataValidation::test_invalid_registration_data PASSED [52%]
test_complete_workflow.py::TestDataValidation::test_invalid_ai_prediction_data PASSED [56%]
test_complete_workflow.py::TestMBTIPersonalization::test_mbti_llm_personalization[INTJ] PASSED [60%]
test_complete_workflow.py::TestMBTIPersonalization::test_mbti_llm_personalization[INTP] PASSED [64%]
... [16 MBTI type tests] ...
test_complete_workflow.py::TestSystemHealth::test_api_gateway_health PASSED [92%]
test_complete_workflow.py::TestSystemHealth::test_ai_model_health PASSED [96%]
test_complete_workflow.py::TestSystemHealth::test_llm_service_health PASSED [100%]

======================== 25 passed in 45.23s ========================
```

## Troubleshooting

### Services Not Running

**Error:** `Connection refused` or `Service unavailable`

**Solution:**
```bash
# Start services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f api-gateway
docker-compose logs -f ai-model
```

### Database Connection Issues

**Error:** `Database connection failed`

**Solution:**
```bash
# Restart database
docker-compose restart postgres

# Check database logs
docker-compose logs postgres
```

### Port Conflicts

**Error:** `Port already in use`

**Solution:**
```bash
# Stop conflicting services
docker-compose down

# Change ports in docker-compose.yml if needed
```

### Test Failures

**Scenario:** Some tests fail

**Solution:**
1. Check service logs: `docker-compose logs [service-name]`
2. Verify all environment variables are set in `.env`
3. Ensure database schema is initialized
4. Run tests individually to isolate issues

### Slow Test Execution

**Solution:**
```bash
# Run specific test suites
pytest test_complete_workflow.py::TestSystemHealth -v

# Skip slow tests
pytest -m "not slow" -v
```

## CI/CD Integration

### GitHub Actions

The tests are automatically run on every push and pull request via GitHub Actions:

```yaml
# .github/workflows/e2e-tests.yml
- name: Run E2E Tests
  run: |
    docker-compose up -d
    sleep 60
    cd tests/e2e
    pip install -r requirements.txt
    pytest -v --html=test-report.html
```

### Jenkins

```groovy
stage('E2E Tests') {
    steps {
        sh 'docker-compose up -d'
        sh 'sleep 60'
        sh 'cd tests/e2e && pip install -r requirements.txt'
        sh 'pytest -v --html=test-report.html'
    }
}
```

## Test Data Cleanup

After tests complete, cleanup test data:

```bash
# Manual cleanup
docker-compose exec postgres psql -U foodsec_user -d foodsecuritynet -c "DELETE FROM users WHERE username LIKE 'testuser_%';"

# Or restart all services for fresh state
docker-compose down -v
docker-compose up -d
```

## Performance Benchmarks

Expected test execution times:
- Authentication tests: ~5 seconds
- Data upload tests: ~3 seconds
- Visualization tests: ~8 seconds
- AI prediction tests: ~15 seconds
- LLM query tests: ~20 seconds
- MBTI personalization (all 16): ~5 minutes
- **Total execution time: ~6-7 minutes**

## Coverage Goals

- **Unit Tests:** >90% code coverage
- **Integration Tests:** All API endpoints tested
- **E2E Tests:** Complete user workflows covered
- **Performance Tests:** Response times < 3s for most endpoints

## Next Steps

1. ✅ Run the E2E tests as described above
2. ✅ Review the HTML test report
3. ✅ Fix any failing tests
4. ✅ Add more test scenarios as needed
5. ✅ Integrate with CI/CD pipeline

## Support

For issues with testing:
- 📧 Email: support@foodsecuritynet.org
- 💬 Discussion Forum: https://github.com/foodsecuritynet/foodsecuritynet/discussions
- 🐛 Issue Tracker: https://github.com/foodsecuritynet/foodsecuritynet/issues

---

**Happy Testing! 🧪✨**
