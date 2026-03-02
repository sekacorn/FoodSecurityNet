import React, { useState } from 'react';
import PropTypes from 'prop-types';
import LLMChat from '../components/LLMChat';
import ResourceMonitor from '../components/ResourceMonitor';

const Troubleshoot = ({ user }) => {
  const [selectedIssue, setSelectedIssue] = useState(null);

  const commonIssues = [
    {
      id: 'pest',
      title: 'Pest Management',
      description: 'Identify and control agricultural pests',
      icon: '🐛',
    },
    {
      id: 'soil',
      title: 'Soil Quality',
      description: 'Improve soil health and composition',
      icon: '🌱',
    },
    {
      id: 'water',
      title: 'Water Management',
      description: 'Optimize irrigation and water usage',
      icon: '💧',
    },
    {
      id: 'disease',
      title: 'Plant Disease',
      description: 'Diagnose and treat crop diseases',
      icon: '🦠',
    },
    {
      id: 'yield',
      title: 'Low Yield',
      description: 'Increase crop productivity',
      icon: '📉',
    },
    {
      id: 'climate',
      title: 'Climate Adaptation',
      description: 'Adapt to changing weather patterns',
      icon: '🌡️',
    },
  ];

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Troubleshoot & Assist</h1>
          <p className="text-gray-600">
            Get AI-powered assistance for your agricultural challenges
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Sidebar - Common Issues */}
          <div className="lg:col-span-1 space-y-6">
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-semibold text-gray-900 mb-4">Common Issues</h3>
              <div className="space-y-2">
                {commonIssues.map((issue) => (
                  <button
                    key={issue.id}
                    onClick={() => setSelectedIssue(issue)}
                    className={`w-full px-4 py-3 rounded-lg border-2 text-left transition-all ${
                      selectedIssue?.id === issue.id
                        ? 'border-primary-600 bg-primary-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <div className="flex items-center space-x-3">
                      <span className="text-2xl">{issue.icon}</span>
                      <div>
                        <div className="font-medium text-gray-900">{issue.title}</div>
                        <div className="text-xs text-gray-600">{issue.description}</div>
                      </div>
                    </div>
                  </button>
                ))}
              </div>
            </div>

            {/* System Resources */}
            <ResourceMonitor refreshInterval={5000} />
          </div>

          {/* Main Content - Chat Interface */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-lg shadow overflow-hidden">
              {selectedIssue && (
                <div className="bg-primary-50 border-b border-primary-200 p-4">
                  <div className="flex items-center space-x-3">
                    <span className="text-3xl">{selectedIssue.icon}</span>
                    <div>
                      <h3 className="font-semibold text-primary-900">
                        {selectedIssue.title}
                      </h3>
                      <p className="text-sm text-primary-700">{selectedIssue.description}</p>
                    </div>
                  </div>
                </div>
              )}

              <div style={{ height: '700px' }}>
                <LLMChat
                  context={selectedIssue ? { issue: selectedIssue.id } : null}
                  user={user}
                />
              </div>
            </div>

            {/* Tips Section */}
            <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-6">
              <h3 className="font-semibold text-blue-900 mb-3">Pro Tips</h3>
              <ul className="space-y-2 text-sm text-blue-800">
                <li className="flex items-start">
                  <svg
                    className="w-5 h-5 text-blue-600 mr-2 flex-shrink-0 mt-0.5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  Be specific about your farming conditions and location for better recommendations
                </li>
                <li className="flex items-start">
                  <svg
                    className="w-5 h-5 text-blue-600 mr-2 flex-shrink-0 mt-0.5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  Include details about soil type, climate, and crop varieties for tailored advice
                </li>
                <li className="flex items-start">
                  <svg
                    className="w-5 h-5 text-blue-600 mr-2 flex-shrink-0 mt-0.5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  Ask follow-up questions to dive deeper into specific recommendations
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

Troubleshoot.propTypes = {
  user: PropTypes.object.isRequired,
};

export default Troubleshoot;
