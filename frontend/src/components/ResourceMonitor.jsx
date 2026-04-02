import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import api from '../services/api';

const ResourceMonitor = ({ refreshInterval = 5000 }) => {
  const [resources, setResources] = useState({
    cpu: 0,
    memory: 0,
    gpu: 0,
    disk: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  useEffect(() => {
    const fetchResources = async () => {
      try {
        const response = await api.get('/system/resources');
        setResources(response.data);
        setError(null);
        setNotice(null);
      } catch (err) {
        const memory = performance?.memory;
        setResources({
          cpu: 0,
          memory: memory?.jsHeapSizeLimit
            ? (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100
            : 0,
          gpu: 0,
          disk: 0,
        });
        setError(null);
        setNotice('Live backend resource metrics unavailable. Showing browser-only memory usage.');
      } finally {
        setLoading(false);
      }
    };

    fetchResources();
    const interval = setInterval(fetchResources, refreshInterval);

    return () => clearInterval(interval);
  }, [refreshInterval]);

  const getStatusColor = (value) => {
    if (value >= 90) return 'text-red-600 bg-red-100';
    if (value >= 70) return 'text-yellow-600 bg-yellow-100';
    return 'text-green-600 bg-green-100';
  };

  const getProgressColor = (value) => {
    if (value >= 90) return 'bg-red-600';
    if (value >= 70) return 'bg-yellow-600';
    return 'bg-green-600';
  };

  const ResourceBar = ({ label, value }) => (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <span className="font-medium text-gray-700">{label}</span>
        <span className={`px-3 py-1 rounded-full text-sm font-semibold ${getStatusColor(value)}`}>
          {value.toFixed(1)}%
        </span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
        <div
          className={`h-full transition-all duration-500 ${getProgressColor(value)}`}
          style={{ width: `${value}%` }}
          role="progressbar"
          aria-label={`${label} usage`}
          aria-valuenow={value}
          aria-valuemin="0"
          aria-valuemax="100"
          aria-valuetext={`${label} usage ${value.toFixed(1)} percent`}
        ></div>
      </div>
    </div>
  );

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="animate-pulse space-y-4">
          <div className="h-4 bg-gray-200 rounded w-1/4"></div>
          <div className="h-3 bg-gray-200 rounded"></div>
          <div className="h-3 bg-gray-200 rounded"></div>
          <div className="h-3 bg-gray-200 rounded"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="text-center text-red-600">
          <svg
            className="w-12 h-12 mx-auto mb-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-semibold text-gray-900">System Resources</h3>
        <div className="flex items-center space-x-2">
          <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
          <span className="text-xs text-gray-500">Live</span>
        </div>
      </div>

      <div className="space-y-6">
        {notice && (
          <div className="rounded-lg border border-yellow-200 bg-yellow-50 px-4 py-3 text-sm text-yellow-800" role="status" aria-live="polite">
            {notice}
          </div>
        )}
        <ResourceBar label="CPU" value={resources.cpu} />
        <ResourceBar label="Memory" value={resources.memory} />
        <ResourceBar label="GPU" value={resources.gpu} />
        <ResourceBar label="Disk" value={resources.disk} />
      </div>

      <div className="mt-6 pt-6 border-t border-gray-200">
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-gray-600">Update Interval</p>
            <p className="font-medium text-gray-900">{refreshInterval / 1000}s</p>
          </div>
          <div>
            <p className="text-gray-600">Status</p>
            <p className="font-medium text-green-600">Monitoring</p>
          </div>
        </div>
      </div>
    </div>
  );
};

ResourceMonitor.propTypes = {
  refreshInterval: PropTypes.number,
};

export default ResourceMonitor;
