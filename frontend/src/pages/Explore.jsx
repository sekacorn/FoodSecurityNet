import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';
import AgriViewer from '../components/AgriViewer';
import ExportTool from '../components/ExportTool';
import api from '../services/api';

const Explore = ({ user }) => {
  const [visualizationType, setVisualizationType] = useState('farm');
  const [viewData, setViewData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDataset, setSelectedDataset] = useState('sample');

  useEffect(() => {
    loadVisualizationData();
  }, [selectedDataset]);

  const loadVisualizationData = async () => {
    setLoading(true);
    try {
      const response = await api.get(`/visualizations/${selectedDataset}`);
      setViewData(response.data.data || generateSampleData());
    } catch (error) {
      console.error('Failed to load visualization data:', error);
      toast.warning('Using sample data for visualization');
      setViewData(generateSampleData());
    } finally {
      setLoading(false);
    }
  };

  const generateSampleData = () => {
    if (visualizationType === 'farm') {
      return Array.from({ length: 10 }, (_, i) => ({
        x: (i % 5) * 2 - 4,
        y: 0,
        z: Math.floor(i / 5) * 2 - 1,
        width: 1.5,
        height: Math.random() * 2 + 1,
        depth: 1.5,
      }));
    } else {
      return [
        { name: 'Wheat', yield: 85, color: '#f59e0b' },
        { name: 'Corn', yield: 92, color: '#eab308' },
        { name: 'Rice', yield: 78, color: '#22c55e' },
        { name: 'Soybeans', yield: 88, color: '#16a34a' },
        { name: 'Barley', yield: 73, color: '#84cc16' },
      ];
    }
  };

  const datasets = [
    { id: 'sample', name: 'Sample Data' },
    { id: 'user', name: 'My Uploaded Data' },
    { id: 'demo', name: 'Demo Dataset' },
  ];

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">3D Exploration</h1>
          <p className="text-gray-600">
            Visualize your agricultural data in interactive 3D environments
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Left Sidebar - Controls */}
          <div className="lg:col-span-1 space-y-6">
            {/* Visualization Type */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-semibold text-gray-900 mb-4">Visualization Type</h3>
              <div className="space-y-2">
                <button
                  onClick={() => {
                    setVisualizationType('farm');
                    setViewData(generateSampleData());
                  }}
                  className={`w-full px-4 py-3 rounded-lg border-2 text-left transition-all ${
                    visualizationType === 'farm'
                      ? 'border-primary-600 bg-primary-50 text-primary-900'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <div className="font-medium">Farm Layout</div>
                  <div className="text-sm text-gray-600">3D plot visualization</div>
                </button>
                <button
                  onClick={() => {
                    setVisualizationType('crops');
                    setViewData(generateSampleData());
                  }}
                  className={`w-full px-4 py-3 rounded-lg border-2 text-left transition-all ${
                    visualizationType === 'crops'
                      ? 'border-primary-600 bg-primary-50 text-primary-900'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <div className="font-medium">Crop Yields</div>
                  <div className="text-sm text-gray-600">Bar chart comparison</div>
                </button>
              </div>
            </div>

            {/* Dataset Selection */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-semibold text-gray-900 mb-4">Dataset</h3>
              <select
                value={selectedDataset}
                onChange={(e) => setSelectedDataset(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              >
                {datasets.map((dataset) => (
                  <option key={dataset.id} value={dataset.id}>
                    {dataset.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Info Panel */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <h3 className="font-semibold text-blue-900 mb-2">Interactive Controls</h3>
              <ul className="text-sm text-blue-800 space-y-1">
                <li>🖱️ Left drag: Rotate view</li>
                <li>🖱️ Right drag: Pan camera</li>
                <li>🔍 Scroll: Zoom in/out</li>
                <li>📸 Export: Save as image</li>
              </ul>
            </div>

            {/* Export */}
            <ExportTool
              data={viewData}
              fileName={`3d-visualization-${visualizationType}`}
            />
          </div>

          {/* Main Viewer */}
          <div className="lg:col-span-3">
            {loading ? (
              <div className="bg-white rounded-lg shadow p-12">
                <div className="text-center">
                  <div className="inline-block animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-primary-600 mb-4"></div>
                  <h3 className="text-xl font-semibold text-gray-900 mb-2">
                    Loading Visualization...
                  </h3>
                  <p className="text-gray-600">Preparing your 3D environment</p>
                </div>
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow p-6">
                <div className="mb-4 flex items-center justify-between">
                  <h2 className="text-xl font-semibold text-gray-900">
                    {visualizationType === 'farm' ? 'Farm Layout View' : 'Crop Yield Comparison'}
                  </h2>
                  <button
                    onClick={loadVisualizationData}
                    className="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
                  >
                    Refresh
                  </button>
                </div>

                <AgriViewer
                  data={viewData}
                  visualizationType={visualizationType}
                  width="100%"
                  height="600px"
                />

                {/* Data Summary */}
                <div className="mt-6 p-4 bg-gray-50 rounded-lg">
                  <h3 className="font-semibold text-gray-900 mb-2">Data Summary</h3>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                    <div>
                      <p className="text-gray-600">Data Points</p>
                      <p className="font-semibold text-gray-900">{viewData.length}</p>
                    </div>
                    <div>
                      <p className="text-gray-600">Visualization</p>
                      <p className="font-semibold text-gray-900">
                        {visualizationType.charAt(0).toUpperCase() + visualizationType.slice(1)}
                      </p>
                    </div>
                    <div>
                      <p className="text-gray-600">Dataset</p>
                      <p className="font-semibold text-gray-900">
                        {datasets.find((d) => d.id === selectedDataset)?.name}
                      </p>
                    </div>
                    <div>
                      <p className="text-gray-600">Status</p>
                      <p className="font-semibold text-green-600">Active</p>
                    </div>
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

Explore.propTypes = {
  user: PropTypes.object.isRequired,
};

export default Explore;
