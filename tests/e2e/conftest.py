"""
Pytest fixtures for E2E tests
"""
import pytest
import requests
import time
from typing import Dict, Any


@pytest.fixture(scope="session")
def base_url():
    """Base URL for the application"""
    return "http://localhost:8080"


@pytest.fixture(scope="session")
def frontend_url():
    """Frontend URL"""
    return "http://localhost:3000"


@pytest.fixture(scope="session")
def ai_model_url():
    """AI model service URL"""
    return "http://localhost:8000"


@pytest.fixture(scope="session")
def llm_service_url():
    """LLM service URL"""
    return "http://localhost:8001"


@pytest.fixture(scope="session")
def wait_for_services(base_url, ai_model_url, llm_service_url):
    """Wait for all services to be ready"""
    services = [
        (base_url, "/actuator/health"),
        (ai_model_url, "/health"),
        (llm_service_url, "/health"),
    ]

    max_retries = 30
    retry_delay = 2

    for service_url, health_path in services:
        for attempt in range(max_retries):
            try:
                response = requests.get(f"{service_url}{health_path}", timeout=5)
                if response.status_code == 200:
                    print(f"✓ Service ready: {service_url}")
                    break
            except requests.exceptions.RequestException:
                if attempt < max_retries - 1:
                    print(f"⏳ Waiting for {service_url}... (attempt {attempt + 1}/{max_retries})")
                    time.sleep(retry_delay)
                else:
                    pytest.skip(f"Service not available: {service_url}")


@pytest.fixture
def test_user_data():
    """Test user registration data"""
    return {
        "username": f"testuser_{int(time.time())}",
        "email": f"test_{int(time.time())}@example.com",
        "password": "SecureP@ssw0rd123",
        "fullName": "Test User"
    }


@pytest.fixture
def registered_user(base_url, test_user_data):
    """Create and return a registered user with auth token"""
    # Register user
    response = requests.post(
        f"{base_url}/api/auth/register",
        json=test_user_data
    )

    if response.status_code not in [200, 201]:
        pytest.skip(f"User registration failed: {response.text}")

    # Login to get token
    login_response = requests.post(
        f"{base_url}/api/auth/login",
        json={
            "username": test_user_data["username"],
            "password": test_user_data["password"]
        }
    )

    if login_response.status_code != 200:
        pytest.skip(f"User login failed: {login_response.text}")

    token_data = login_response.json()

    return {
        "user_data": test_user_data,
        "access_token": token_data.get("accessToken"),
        "refresh_token": token_data.get("refreshToken"),
        "user_id": token_data.get("userId")
    }


@pytest.fixture
def auth_headers(registered_user):
    """Return authorization headers with JWT token"""
    return {
        "Authorization": f"Bearer {registered_user['access_token']}",
        "Content-Type": "application/json"
    }


@pytest.fixture
def sample_agricultural_data():
    """Sample agricultural data for testing"""
    return {
        "region": "Sub-Saharan Africa",
        "country": "Kenya",
        "latitude": -1.286389,
        "longitude": 36.817223,
        "cropType": "maize",
        "cropYield": 2500.50,
        "soilType": "Clay Loam",
        "soilPh": 6.5,
        "soilNitrogen": 45.2,
        "soilPhosphorus": 12.8,
        "soilPotassium": 180.5,
        "irrigationType": "drip",
        "plantingDate": "2024-03-15",
        "harvestDate": "2024-07-20",
        "dataSource": "USER"
    }


@pytest.fixture
def sample_environmental_data():
    """Sample environmental data for testing"""
    return {
        "region": "Sub-Saharan Africa",
        "latitude": -1.286389,
        "longitude": 36.817223,
        "temperatureAvg": 22.5,
        "temperatureMin": 15.0,
        "temperatureMax": 30.0,
        "rainfall": 850.5,
        "humidity": 65.0,
        "windSpeed": 12.5,
        "solarRadiation": 5500.0,
        "climateZone": "Tropical",
        "dataSource": "NOAA",
        "observationDate": "2024-03-15"
    }


@pytest.fixture
def sample_socioeconomic_data():
    """Sample socioeconomic data for testing"""
    return {
        "region": "Sub-Saharan Africa",
        "country": "Kenya",
        "incomeLevel": "low",
        "marketAccessScore": 3.5,
        "farmSize": 2.5,
        "householdSize": 6,
        "educationLevel": "secondary",
        "infrastructureScore": 4.2,
        "foodSecurityIndex": 3.8,
        "dataSource": "SURVEY",
        "surveyDate": "2024-01-01"
    }


@pytest.fixture
def sample_ai_prediction_request():
    """Sample AI prediction request data"""
    return {
        "agricultural_data": {
            "crop_type": "maize",
            "soil_ph": 6.5,
            "soil_nitrogen": 45.2,
            "soil_phosphorus": 12.8,
            "soil_potassium": 180.5,
            "irrigation_type": "drip",
            "farm_size": 2.5
        },
        "environmental_data": {
            "temperature_avg": 22.5,
            "rainfall": 850.5,
            "humidity": 65.0,
            "climate_zone": "tropical"
        },
        "socioeconomic_data": {
            "income_level": "low",
            "market_access_score": 3.5,
            "education_level": "secondary"
        }
    }


@pytest.fixture
def sample_llm_query():
    """Sample LLM query data"""
    return {
        "query": "What crops should I plant this season in Kenya?",
        "context": {
            "region": "Kenya",
            "season": "spring",
            "soil_type": "clay_loam"
        }
    }
