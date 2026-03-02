import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';
import DataUpload from '../components/DataUpload';
import AgriDetails from '../components/AgriDetails';
import ExportTool from '../components/ExportTool';
import api from '../services/api';

const Analyze = ({ user }) => {
  const [analyzing, setAnalyzing] = useState(false);
  const [recommendations, setRecommendations] = useState(null);
  const [uploadedData, setUploadedData] = useState(null);

  const handleUploadSuccess = async ({ file, content }) => {
    setUploadedData(content);
    setAnalyzing(true);

    try {
      const response = await api.post('/analyze', {
        data: content,
        user_id: user.id,
        mbti: user.mbti || 'ISTJ',
      });

      setRecommendations(response.data.recommendations || []);
      toast.success('Analysis completed successfully!');
    } catch (error) {
      console.error('Analysis error:', error);
      toast.error('Failed to analyze data. Please try again.');
    } finally {
      setAnalyzing(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Farming Analysis</h1>
          <p className="text-gray-600">
            Upload your agricultural data to receive AI-powered recommendations tailored to your
            needs
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column - Upload & Settings */}
          <div className="lg:col-span-1 space-y-6">
            {/* Data Upload */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Upload Data</h2>
              <DataUpload
                onUploadSuccess={handleUploadSuccess}
                acceptedFormats={['.csv', '.json']}
                maxSize={10485760}
              />
            </div>

            {/* User MBTI Info */}
            {user.mbti && (
              <div className="bg-primary-50 border border-primary-200 rounded-lg p-6">
                <h3 className="font-semibold text-primary-900 mb-2">Personalization</h3>
                <p className="text-sm text-primary-700 mb-3">
                  Recommendations are tailored for your personality type:
                </p>
                <div className="inline-flex items-center px-3 py-2 bg-primary-600 text-white rounded-lg font-semibold">
                  {user.mbti}
                </div>
              </div>
            )}

            {/* Export Tool */}
            {recommendations && (
              <ExportTool
                data={recommendations}
                fileName={`farming-analysis-${new Date().toISOString().split('T')[0]}`}
              />
            )}
          </div>

          {/* Right Column - Results */}
          <div className="lg:col-span-2">
            {analyzing ? (
              <div className="bg-white rounded-lg shadow p-12">
                <div className="text-center">
                  <div className="inline-block animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-primary-600 mb-4"></div>
                  <h3 className="text-xl font-semibold text-gray-900 mb-2">
                    Analyzing Your Data...
                  </h3>
                  <p className="text-gray-600">
                    Our AI is processing your agricultural data and generating personalized
                    recommendations
                  </p>
                  <div className="mt-6 max-w-md mx-auto">
                    <div className="flex justify-between text-sm text-gray-600 mb-2">
                      <span>Progress</span>
                      <span>Processing...</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div className="bg-primary-600 h-2 rounded-full animate-pulse-slow w-3/4"></div>
                    </div>
                  </div>
                </div>
              </div>
            ) : recommendations ? (
              <div>
                <div className="bg-white rounded-lg shadow p-6 mb-6">
                  <div className="flex items-center justify-between mb-4">
                    <h2 className="text-2xl font-bold text-gray-900">
                      Analysis Results
                    </h2>
                    <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
                      {recommendations.length} Recommendations
                    </span>
                  </div>
                  <p className="text-gray-600">
                    Based on your data, we have generated the following personalized farming
                    recommendations
                  </p>
                </div>

                <AgriDetails recommendations={recommendations} userMBTI={user.mbti || 'ISTJ'} />
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow p-12">
                <div className="text-center">
                  <svg
                    className="w-24 h-24 mx-auto mb-6 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                    />
                  </svg>
                  <h3 className="text-xl font-semibold text-gray-900 mb-2">
                    No Analysis Yet
                  </h3>
                  <p className="text-gray-600 mb-6">
                    Upload your agricultural data to get started with AI-powered recommendations
                  </p>
                  <div className="max-w-md mx-auto text-left bg-gray-50 rounded-lg p-6">
                    <h4 className="font-semibold text-gray-900 mb-3">Supported Data:</h4>
                    <ul className="space-y-2 text-sm text-gray-600">
                      <li className="flex items-start">
                        <svg className="w-5 h-5 text-green-600 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                        Soil composition and nutrient levels
                      </li>
                      <li className="flex items-start">
                        <svg className="w-5 h-5 text-green-600 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                        Crop yield and production data
                      </li>
                      <li className="flex items-start">
                        <svg className="w-5 h-5 text-green-600 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                        Weather and climate information
                      </li>
                      <li className="flex items-start">
                        <svg className="w-5 h-5 text-green-600 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                        Irrigation and water usage metrics
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

Analyze.propTypes = {
  user: PropTypes.object.isRequired,
};

export default Analyze;
