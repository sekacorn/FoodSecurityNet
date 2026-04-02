import React from 'react';
import PropTypes from 'prop-types';
import sanitizeHtml from 'sanitize-html';

const AgriDetails = ({ recommendations }) => {
  if (!recommendations || recommendations.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="text-center text-gray-500">
          <svg
            className="w-16 h-16 mx-auto mb-4 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
          <p className="text-lg font-medium">No recommendations available</p>
          <p className="text-sm mt-2">Upload data to get farming recommendations</p>
        </div>
      </div>
    );
  }

  const sanitizeContent = (content) => {
    return sanitizeHtml(content, {
      allowedTags: ['b', 'i', 'em', 'strong', 'p', 'br', 'ul', 'ol', 'li'],
      allowedAttributes: {},
    });
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-6">
        {recommendations.map((rec, index) => (
          <div
            key={index}
            className="bg-white rounded-lg shadow-md overflow-hidden border-l-4 border-primary-600"
          >
            <div className="p-6">
              {/* Header */}
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <h3 className="text-xl font-bold text-gray-900 mb-2">
                    {rec.title || `Recommendation ${index + 1}`}
                  </h3>
                  {rec.category && (
                    <span className="inline-block px-2 py-1 text-xs font-semibold text-gray-700 bg-gray-200 rounded-full">
                      {rec.category}
                    </span>
                  )}
                </div>
                {rec.priority && (
                  <div className={`ml-4 px-3 py-1 rounded-full text-sm font-medium ${getPriorityColor(rec.priority)}`}>
                    {rec.priority}
                  </div>
                )}
              </div>

              {/* Description */}
              <div
                className="prose prose-sm max-w-none mb-4 text-gray-700"
                dangerouslySetInnerHTML={{ __html: sanitizeContent(rec.description || rec.content || '') }}
              />

              {/* Metrics */}
              {rec.metrics && (
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4 p-4 bg-gray-50 rounded-lg">
                  {Object.entries(rec.metrics).map(([key, value]) => (
                    <div key={key} className="text-center">
                      <p className="text-2xl font-bold text-primary-600">{value}</p>
                      <p className="text-xs text-gray-600 uppercase">{key.replace(/_/g, ' ')}</p>
                    </div>
                  ))}
                </div>
              )}

              {rec.benefits && rec.benefits.length > 0 && (
                <div className="mt-4">
                  <h4 className="font-semibold text-gray-800 mb-2">Expected Benefits</h4>
                  <ul className="space-y-2">
                    {rec.benefits.map((benefit, idx) => (
                      <li key={idx} className="flex items-start">
                        <svg
                          className="w-5 h-5 text-primary-600 mr-2 flex-shrink-0 mt-0.5"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                          />
                        </svg>
                        <span className="text-gray-700">{benefit}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {rec.actions && rec.actions.length > 0 && (
                <div className="mt-4">
                  <h4 className="font-semibold text-gray-800 mb-2">Recommended Actions</h4>
                  <ol className="space-y-2 list-decimal list-inside">
                    {rec.actions.map((action, idx) => (
                      <li key={idx} className="text-gray-700">
                        {action}
                      </li>
                    ))}
                  </ol>
                </div>
              )}

              {/* Timeline */}
              {rec.timeline && (
                <div className="mt-4 flex items-center text-sm text-gray-600">
                  <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  Expected timeline: <span className="font-medium ml-1">{rec.timeline}</span>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const getPriorityColor = (priority) => {
  const colors = {
    high: 'bg-red-100 text-red-800',
    medium: 'bg-yellow-100 text-yellow-800',
    low: 'bg-green-100 text-green-800',
  };
  return colors[priority.toLowerCase()] || 'bg-gray-100 text-gray-800';
};

AgriDetails.propTypes = {
  recommendations: PropTypes.arrayOf(
    PropTypes.shape({
      title: PropTypes.string,
      category: PropTypes.string,
      description: PropTypes.string,
      content: PropTypes.string,
      priority: PropTypes.string,
      metrics: PropTypes.object,
      benefits: PropTypes.arrayOf(PropTypes.string),
      actions: PropTypes.arrayOf(PropTypes.string),
      timeline: PropTypes.string,
    })
  ).isRequired,
};

export default AgriDetails;
