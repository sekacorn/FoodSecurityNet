import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';

const formats = [
  {
    value: 'png',
    label: 'PNG Image',
    icon: (
      <svg className="h-7 w-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={1.8}
          d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2 1.586-1.586a2 2 0 012.828 0L20 14m-8-8h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
        />
      </svg>
    ),
  },
  {
    value: 'svg',
    label: 'SVG Vector',
    icon: (
      <svg className="h-7 w-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={1.8}
          d="M7 7h10v10H7zM7 12h10M12 7v10"
        />
      </svg>
    ),
  },
  {
    value: 'stl',
    label: 'STL 3D Model',
    icon: (
      <svg className="h-7 w-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={1.8}
          d="M12 4l7 4-7 4-7-4 7-4zm7 4v8l-7 4m0-8L5 8m7 4v8"
        />
      </svg>
    ),
  },
  {
    value: 'json',
    label: 'JSON Data',
    icon: (
      <svg className="h-7 w-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={1.8}
          d="M9 4H7a2 2 0 00-2 2v2m0 8v2a2 2 0 002 2h2m6-16h2a2 2 0 012 2v2m0 8v2a2 2 0 01-2 2h-2M10 9l-2 3 2 3m4-6 2 3-2 3"
        />
      </svg>
    ),
  },
  {
    value: 'csv',
    label: 'CSV Data',
    icon: (
      <svg className="h-7 w-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={1.8}
          d="M9 17v-6m3 6V7m3 10v-4M8 3h8l5 5v11a2 2 0 01-2 2H8a2 2 0 01-2-2V5a2 2 0 012-2z"
        />
      </svg>
    ),
  },
];

const ExportTool = ({ data, fileName = 'export', canvasRef = null }) => {
  const [exporting, setExporting] = useState(false);
  const [selectedFormat, setSelectedFormat] = useState('png');

  const exportAsPNG = async () => {
    try {
      if (canvasRef && canvasRef.current) {
        const canvas = canvasRef.current;
        const dataUrl = canvas.toDataURL('image/png');
        downloadFile(dataUrl, `${fileName}.png`);
      } else {
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

      let svgContent = '<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600">\n';
      svgContent += '  <rect width="800" height="600" fill="#f0f0f0"/>\n';

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

      let stlContent = 'solid exported_model\n';

      data.forEach((item) => {
        const x = item.x || 0;
        const y = item.y || 0;
        const z = item.z || 0;
        const size = 1;

        stlContent += '  facet normal 0 0 1\n';
        stlContent += '    outer loop\n';
        stlContent += `      vertex ${x} ${y} ${z + size}\n`;
        stlContent += `      vertex ${x + size} ${y} ${z + size}\n`;
        stlContent += `      vertex ${x + size} ${y + size} ${z + size}\n`;
        stlContent += '    endloop\n';
        stlContent += '  endfacet\n';
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

      const headers = data.length > 0 ? Object.keys(data[0]) : [];
      let csvContent = `${headers.join(',')}\n`;

      data.forEach((row) => {
        const values = headers.map((header) => {
          const value = row[header];
          if (typeof value === 'string' && (value.includes(',') || value.includes('"'))) {
            return `"${value.replace(/"/g, '""')}"`;
          }
          return value;
        });
        csvContent += `${values.join(',')}\n`;
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

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-5 mb-6">
        {formats.map((format) => (
          <button
            key={format.value}
            onClick={() => setSelectedFormat(format.value)}
            className={`flex min-h-[120px] flex-col items-center justify-center rounded-lg border-2 p-4 text-center transition-all ${
              selectedFormat === format.value
                ? 'border-primary-600 bg-primary-50'
                : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-full bg-gray-100 text-gray-700">
              {format.icon}
            </div>
            <div className="text-sm font-medium leading-5 text-gray-900">{format.label}</div>
          </button>
        ))}
      </div>

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
