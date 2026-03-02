import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';

const ExportTool = ({ data, fileName = 'export', canvasRef = null }) => {
  const [exporting, setExporting] = useState(false);
  const [selectedFormat, setSelectedFormat] = useState('png');

  const formats = [
    { value: 'png', label: 'PNG Image', icon: '🖼️' },
    { value: 'svg', label: 'SVG Vector', icon: '📐' },
    { value: 'stl', label: 'STL 3D Model', icon: '🔷' },
    { value: 'json', label: 'JSON Data', icon: '📄' },
    { value: 'csv', label: 'CSV Data', icon: '📊' },
  ];

  const exportAsPNG = async () => {
    try {
      if (canvasRef && canvasRef.current) {
        const canvas = canvasRef.current;
        const dataUrl = canvas.toDataURL('image/png');
        downloadFile(dataUrl, `${fileName}.png`);
      } else {
        // Fallback: export as data URL
        toast.warning('No canvas available. Exporting data as JSON instead.');
        exportAsJSON();
      }
    } catch (error) {
      console.error('PNG export error:', error);
      toast.error('Failed to export as PNG');
    }
  };

  const exportAsSVG = () => {
    try {
      if (!data) {
        toast.error('No data available to export');
        return;
      }

      // Create simple SVG from data
      let svgContent = '<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600">\n';
      svgContent += '  <rect width="800" height="600" fill="#f0f0f0"/>\n';

      // Add data visualization (simplified)
      if (Array.isArray(data)) {
        data.forEach((item, index) => {
          const x = (index * 50) % 800;
          const y = 300 + (item.value || 0) * 2;
          svgContent += `  <circle cx="${x}" cy="${y}" r="5" fill="#22c55e"/>\n`;
        });
      }

      svgContent += '</svg>';

      const blob = new Blob([svgContent], { type: 'image/svg+xml' });
      const url = URL.createObjectURL(blob);
      downloadFile(url, `${fileName}.svg`);
      URL.revokeObjectURL(url);
    } catch (error) {
      console.error('SVG export error:', error);
      toast.error('Failed to export as SVG');
    }
  };

  const exportAsSTL = () => {
    try {
      if (!data || !Array.isArray(data)) {
        toast.error('No valid 3D data available to export');
        return;
      }

      // Create simple STL file (ASCII format)
      let stlContent = 'solid exported_model\n';

      // Generate triangles from data (simplified)
      data.forEach((item) => {
        const x = item.x || 0;
        const y = item.y || 0;
        const z = item.z || 0;
        const size = 1;

        // Create a simple cube face
        stlContent += `  facet normal 0 0 1\n`;
        stlContent += `    outer loop\n`;
        stlContent += `      vertex ${x} ${y} ${z + size}\n`;
        stlContent += `      vertex ${x + size} ${y} ${z + size}\n`;
        stlContent += `      vertex ${x + size} ${y + size} ${z + size}\n`;
        stlContent += `    endloop\n`;
        stlContent += `  endfacet\n`;
      });

      stlContent += 'endsolid exported_model\n';

      const blob = new Blob([stlContent], { type: 'application/sla' });
      const url = URL.createObjectURL(blob);
      downloadFile(url, `${fileName}.stl`);
      URL.revokeObjectURL(url);
    } catch (error) {
      console.error('STL export error:', error);
      toast.error('Failed to export as STL');
    }
  };

  const exportAsJSON = () => {
    try {
      if (!data) {
        toast.error('No data available to export');
        return;
      }

      const jsonContent = JSON.stringify(data, null, 2);
      const blob = new Blob([jsonContent], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      downloadFile(url, `${fileName}.json`);
      URL.revokeObjectURL(url);
    } catch (error) {
      console.error('JSON export error:', error);
      toast.error('Failed to export as JSON');
    }
  };

  const exportAsCSV = () => {
    try {
      if (!data || !Array.isArray(data)) {
        toast.error('No valid data available to export as CSV');
        return;
      }

      // Get headers from first object
      const headers = data.length > 0 ? Object.keys(data[0]) : [];

      // Create CSV content
      let csvContent = headers.join(',') + '\n';

      data.forEach((row) => {
        const values = headers.map((header) => {
          const value = row[header];
          // Escape commas and quotes
          if (typeof value === 'string' && (value.includes(',') || value.includes('"'))) {
            return `"${value.replace(/"/g, '""')}"`;
          }
          return value;
        });
        csvContent += values.join(',') + '\n';
      });

      const blob = new Blob([csvContent], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      downloadFile(url, `${fileName}.csv`);
      URL.revokeObjectURL(url);
    } catch (error) {
      console.error('CSV export error:', error);
      toast.error('Failed to export as CSV');
    }
  };

  const downloadFile = (url, filename) => {
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleExport = async () => {
    setExporting(true);

    try {
      switch (selectedFormat) {
        case 'png':
          await exportAsPNG();
          break;
        case 'svg':
          exportAsSVG();
          break;
        case 'stl':
          exportAsSTL();
          break;
        case 'json':
          exportAsJSON();
          break;
        case 'csv':
          exportAsCSV();
          break;
        default:
          toast.error('Unknown export format');
      }

      toast.success(`Exported as ${selectedFormat.toUpperCase()} successfully!`);
    } catch (error) {
      console.error('Export error:', error);
      toast.error('Export failed');
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Export Data</h3>

      {/* Format Selection */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3 mb-6">
        {formats.map((format) => (
          <button
            key={format.value}
            onClick={() => setSelectedFormat(format.value)}
            className={`p-4 rounded-lg border-2 transition-all ${
              selectedFormat === format.value
                ? 'border-primary-600 bg-primary-50'
                : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <div className="text-3xl mb-2">{format.icon}</div>
            <div className="text-sm font-medium text-gray-900">{format.label}</div>
          </button>
        ))}
      </div>

      {/* File Name Input */}
      <div className="mb-6">
        <label htmlFor="export-filename" className="block text-sm font-medium text-gray-700 mb-2">
          File Name
        </label>
        <input
          id="export-filename"
          type="text"
          value={fileName}
          readOnly
          className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50"
        />
      </div>

      {/* Export Button */}
      <button
        onClick={handleExport}
        disabled={exporting || !data}
        className="w-full px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors font-medium"
      >
        {exporting ? (
          <span className="flex items-center justify-center">
            <svg
              className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              ></circle>
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              ></path>
            </svg>
            Exporting...
          </span>
        ) : (
          `Export as ${selectedFormat.toUpperCase()}`
        )}
      </button>

      {/* Info Text */}
      <p className="mt-4 text-xs text-gray-500 text-center">
        Export your data in various formats for use in other applications
      </p>
    </div>
  );
};

ExportTool.propTypes = {
  data: PropTypes.any,
  fileName: PropTypes.string,
  canvasRef: PropTypes.object,
};

export default ExportTool;
