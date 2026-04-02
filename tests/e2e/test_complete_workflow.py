"""
Complete end-to-end workflow test for FoodSecurityNet
Tests the entire user journey from registration to data visualization
"""
import pytest
import requests
import time


class TestCompleteUserWorkflow:
    """Test complete user workflow from registration to visualization"""

    def test_01_user_registration(self, base_url, test_user_data):
        """Test user registration"""
        response = requests.post(
            f"{base_url}/api/auth/register",
            json=test_user_data,
            timeout=10
        )

        assert response.status_code in [200, 201], f"Registration failed: {response.text}"
        data = response.json()
        assert "userId" in data or "id" in data, "User ID not returned"
        assert "message" in data or "accessToken" in data, "No confirmation message"
        print(f"✓ User registered successfully: {test_user_data['username']}")

    def test_02_user_login(self, base_url, test_user_data):
        """Test user login"""
        response = requests.post(
            f"{base_url}/api/auth/login",
            json={
                "username": test_user_data["username"],
                "password": test_user_data["password"]
            },
            timeout=10
        )

        assert response.status_code == 200, f"Login failed: {response.text}"
        data = response.json()
        assert "accessToken" in data, "Access token not returned"
        assert "refreshToken" in data, "Refresh token not returned"
        print(f"✓ User logged in successfully")
        print(f"  Access Token: {data['accessToken'][:20]}...")

    def test_03_get_current_user(self, base_url, auth_headers):
        """Test getting current user information"""
        response = requests.get(
            f"{base_url}/api/auth/me",
            headers=auth_headers,
            timeout=10
        )

        assert response.status_code == 200, f"Get user failed: {response.text}"
        data = response.json()
        assert "username" in data, "Username not returned"
        assert "email" in data, "Email not returned"
        print(f"✓ Current user retrieved: {data['username']}")

    def test_04_upload_agricultural_data(self, base_url, auth_headers, sample_agricultural_data):
        """Test uploading agricultural data"""
        response = requests.post(
            f"{base_url}/api/data/upload/json",
            headers=auth_headers,
            json=sample_agricultural_data,
            timeout=10
        )

        assert response.status_code in [200, 201], f"Data upload failed: {response.text}"
        data = response.json()
        assert "id" in data or "dataId" in data, "Data ID not returned"
        print(f"✓ Agricultural data uploaded successfully")
        return data.get("id") or data.get("dataId")

    def test_05_get_agricultural_data(self, base_url, auth_headers):
        """Test retrieving agricultural data"""
        # First upload data
        upload_response = requests.post(
            f"{base_url}/api/data/upload/json",
            headers=auth_headers,
            json={
                "region": "Test Region",
                "cropType": "wheat",
                "cropYield": 3000
            },
            timeout=10
        )

        if upload_response.status_code in [200, 201]:
            data_id = upload_response.json().get("id") or upload_response.json().get("dataId")

            # Retrieve the data
            response = requests.get(
                f"{base_url}/api/data/{data_id}",
                headers=auth_headers,
                timeout=10
            )

            assert response.status_code == 200, f"Data retrieval failed: {response.text}"
            data = response.json()
            assert data.get("cropType") == "wheat", "Crop type mismatch"
            print(f"✓ Agricultural data retrieved successfully")

    def test_06_create_visualization(self, base_url, auth_headers):
        """Test creating a 3D visualization"""
        visualization_data = {
            "title": "Test Farm Visualization",
            "description": "E2E test visualization",
            "visualizationType": "3D_MAP",
            "dataQuery": {
                "region": "Sub-Saharan Africa",
                "cropType": "maize"
            },
            "visualizationConfig": {
                "colorScheme": "viridis",
                "showLegend": True
            }
        }

        response = requests.post(
            f"{base_url}/api/visualizations/create",
            headers=auth_headers,
            json=visualization_data,
            timeout=15
        )

        assert response.status_code in [200, 201], f"Visualization creation failed: {response.text}"
        data = response.json()
        assert "id" in data or "visualizationId" in data, "Visualization ID not returned"
        print(f"✓ Visualization created successfully")
        return data.get("id") or data.get("visualizationId")

    def test_07_ai_prediction(self, ai_model_url, sample_ai_prediction_request):
        """Test AI farming prediction"""
        response = requests.post(
            f"{ai_model_url}/predict",
            json=sample_ai_prediction_request,
            timeout=30
        )

        assert response.status_code == 200, f"AI prediction failed: {response.text}"
        data = response.json()
        assert "recommendations" in data, "Recommendations not returned"
        assert "confidence_scores" in data, "Confidence scores not returned"

        recommendations = data["recommendations"]
        assert "crop_recommendations" in recommendations, "Crop recommendations missing"
        assert "irrigation_recommendations" in recommendations, "Irrigation recommendations missing"

        print(f"✓ AI prediction successful")
        print(f"  Top crop: {recommendations['crop_recommendations'][0]}")
        print(f"  Top irrigation: {recommendations['irrigation_recommendations'][0]}")

    def test_08_llm_query(self, llm_service_url, sample_llm_query):
        """Test LLM natural language query"""
        response = requests.post(
            f"{llm_service_url}/api/query",
            json=sample_llm_query,
            timeout=30
        )

        assert response.status_code == 200, f"LLM query failed: {response.text}"
        data = response.json()
        assert "response" in data, "Response not returned"
        assert len(data["response"]) > 0, "Empty response"

        print(f"✓ LLM query successful")
        print(f"  Query: {sample_llm_query['query']}")
        print(f"  Response: {data['response'][:100]}...")

    def test_09_resource_monitoring(self, ai_model_url):
        """Test resource monitoring endpoint"""
        response = requests.get(
            f"{ai_model_url}/resources/check",
            timeout=10
        )

        assert response.status_code == 200, f"Resource check failed: {response.text}"
        data = response.json()
        assert "cpu_count" in data, "CPU count not returned"
        assert "memory_total" in data, "Memory total not returned"
        assert "gpu_available" in data, "GPU availability not returned"

        print(f"✓ Resource monitoring successful")
        print(f"  CPU cores: {data['cpu_count']}")
        print(f"  Memory: {data['memory_total'] / (1024**3):.2f} GB")
        print(f"  GPU available: {data['gpu_available']}")

    def test_10_create_annotation(self, base_url, auth_headers):
        """Test creating an annotation"""
        annotation_data = {
            "annotationType": "NOTE",
            "content": "This area shows high crop yield",
            "position": {"x": 10, "y": 20, "z": 0},
            "metadata": {"color": "#FF0000"}
        }

        response = requests.post(
            f"{base_url}/api/annotations",
            headers=auth_headers,
            json=annotation_data,
            timeout=10
        )

        assert response.status_code in [200, 201], f"Annotation creation failed: {response.text}"
        data = response.json()
        assert "id" in data or "annotationId" in data, "Annotation ID not returned"
        print(f"✓ Annotation created successfully")

    def test_11_token_refresh(self, base_url, registered_user):
        """Test JWT token refresh"""
        response = requests.post(
            f"{base_url}/api/auth/refresh",
            json={
                "refreshToken": registered_user["refresh_token"]
            },
            timeout=10
        )

        assert response.status_code == 200, f"Token refresh failed: {response.text}"
        data = response.json()
        assert "accessToken" in data, "New access token not returned"
        assert data["accessToken"] != registered_user["access_token"], "Token not refreshed"
        print(f"✓ Token refreshed successfully")

    def test_12_logout(self, base_url, auth_headers):
        """Test user logout"""
        response = requests.post(
            f"{base_url}/api/auth/logout",
            headers=auth_headers,
            timeout=10
        )

        assert response.status_code in [200, 204], f"Logout failed: {response.text}"
        print(f"✓ User logged out successfully")


class TestDataValidation:
    """Test data validation and error handling"""

    def test_invalid_registration_data(self, base_url):
        """Test registration with invalid data"""
        invalid_data = {
            "username": "ab",  # Too short
            "email": "invalid-email",  # Invalid format
            "password": "weak"  # Too weak
        }

        response = requests.post(
            f"{base_url}/api/auth/register",
            json=invalid_data,
            timeout=10
        )

        assert response.status_code in [400, 422], "Should reject invalid data"
        print(f"✓ Invalid registration data rejected correctly")

    def test_invalid_ai_prediction_data(self, ai_model_url):
        """Test AI prediction with invalid data"""
        invalid_data = {
            "agricultural_data": {
                "soil_ph": 150  # Invalid pH value
            }
        }

        response = requests.post(
            f"{ai_model_url}/predict",
            json=invalid_data,
            timeout=10
        )

        assert response.status_code in [400, 422], "Should reject invalid prediction data"
        print(f"✓ Invalid prediction data rejected correctly")


class TestSystemHealth:
    """Test system health and monitoring"""

    def test_api_gateway_health(self, base_url):
        """Test API gateway health check"""
        response = requests.get(
            f"{base_url}/actuator/health",
            timeout=10
        )

        assert response.status_code == 200, "API Gateway unhealthy"
        data = response.json()
        assert data.get("status") == "UP", "API Gateway status not UP"
        print(f"✓ API Gateway health check passed")

    def test_ai_model_health(self, ai_model_url):
        """Test AI model service health"""
        response = requests.get(
            f"{ai_model_url}/health",
            timeout=10
        )

        assert response.status_code == 200, "AI Model service unhealthy"
        data = response.json()
        assert data.get("status") == "healthy", "AI Model status not healthy"
        print(f"✓ AI Model service health check passed")

    def test_llm_service_health(self, llm_service_url):
        """Test LLM service health"""
        response = requests.get(
            f"{llm_service_url}/health",
            timeout=10
        )

        assert response.status_code == 200, "LLM service unhealthy"
        data = response.json()
        assert data.get("status") == "healthy", "LLM status not healthy"
        print(f"✓ LLM service health check passed")


if __name__ == "__main__":
    pytest.main([__file__, "-v", "-s"])
