import React, { useState, useRef } from 'react';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';

const AnnotationTool = ({ imageUrl, onAnnotationSave, existingAnnotations = [] }) => {
  const [annotations, setAnnotations] = useState(existingAnnotations);
  const [isDrawing, setIsDrawing] = useState(false);
  const [currentAnnotation, setCurrentAnnotation] = useState(null);
  const [selectedTool, setSelectedTool] = useState('rectangle');
  const [selectedColor, setSelectedColor] = useState('#ff0000');
  const [annotationText, setAnnotationText] = useState('');
  const canvasRef = useRef(null);
  const imageRef = useRef(null);

  const tools = [
    { name: 'rectangle', icon: '▭', label: 'Rectangle' },
    { name: 'circle', icon: '○', label: 'Circle' },
    { name: 'arrow', icon: '➔', label: 'Arrow' },
    { name: 'text', icon: 'T', label: 'Text' },
  ];

  const colors = ['#ff0000', '#00ff00', '#0000ff', '#ffff00', '#ff00ff', '#00ffff', '#ffffff'];

  const handleMouseDown = (e) => {
    if (!canvasRef.current) return;

    const rect = canvasRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    setIsDrawing(true);
    setCurrentAnnotation({
      tool: selectedTool,
      color: selectedColor,
      startX: x,
      startY: y,
      endX: x,
      endY: y,
      text: annotationText,
    });
  };

  const handleMouseMove = (e) => {
    if (!isDrawing || !canvasRef.current || !currentAnnotation) return;

    const rect = canvasRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    setCurrentAnnotation({
      ...currentAnnotation,
      endX: x,
      endY: y,
    });

    redrawCanvas();
  };

  const handleMouseUp = () => {
    if (!isDrawing || !currentAnnotation) return;

    setIsDrawing(false);
    setAnnotations([...annotations, currentAnnotation]);
    setCurrentAnnotation(null);
    setAnnotationText('');
    redrawCanvas();
  };

  const redrawCanvas = () => {
    const canvas = canvasRef.current;
    const image = imageRef.current;
    if (!canvas || !image) return;

    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.drawImage(image, 0, 0, canvas.width, canvas.height);

    // Draw all existing annotations
    annotations.forEach((annotation) => drawAnnotation(ctx, annotation));

    // Draw current annotation being created
    if (currentAnnotation) {
      drawAnnotation(ctx, currentAnnotation);
    }
  };

  const drawAnnotation = (ctx, annotation) => {
    ctx.strokeStyle = annotation.color;
    ctx.lineWidth = 2;
    ctx.fillStyle = annotation.color;

    const width = annotation.endX - annotation.startX;
    const height = annotation.endY - annotation.startY;

    switch (annotation.tool) {
      case 'rectangle':
        ctx.strokeRect(annotation.startX, annotation.startY, width, height);
        break;

      case 'circle':
        const radius = Math.sqrt(width * width + height * height);
        ctx.beginPath();
        ctx.arc(annotation.startX, annotation.startY, radius, 0, 2 * Math.PI);
        ctx.stroke();
        break;

      case 'arrow':
        drawArrow(ctx, annotation.startX, annotation.startY, annotation.endX, annotation.endY);
        break;

      case 'text':
        ctx.font = '16px Arial';
        ctx.fillText(annotation.text || 'Text', annotation.startX, annotation.startY);
        break;

      default:
        break;
    }
  };

  const drawArrow = (ctx, fromX, fromY, toX, toY) => {
    const headLength = 10;
    const angle = Math.atan2(toY - fromY, toX - fromX);

    ctx.beginPath();
    ctx.moveTo(fromX, fromY);
    ctx.lineTo(toX, toY);
    ctx.lineTo(
      toX - headLength * Math.cos(angle - Math.PI / 6),
      toY - headLength * Math.sin(angle - Math.PI / 6)
    );
    ctx.moveTo(toX, toY);
    ctx.lineTo(
      toX - headLength * Math.cos(angle + Math.PI / 6),
      toY - headLength * Math.sin(angle + Math.PI / 6)
    );
    ctx.stroke();
  };

  const handleImageLoad = () => {
    const canvas = canvasRef.current;
    const image = imageRef.current;
    if (!canvas || !image) return;

    canvas.width = image.width;
    canvas.height = image.height;
    redrawCanvas();
  };

  const handleSave = () => {
    if (onAnnotationSave) {
      onAnnotationSave(annotations);
      toast.success('Annotations saved successfully!');
    }
  };

  const handleClear = () => {
    setAnnotations([]);
    setCurrentAnnotation(null);
    redrawCanvas();
  };

  const handleUndo = () => {
    if (annotations.length === 0) return;
    setAnnotations(annotations.slice(0, -1));
    setTimeout(redrawCanvas, 0);
  };

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="bg-white rounded-lg shadow p-4">
        <div className="flex flex-wrap items-center gap-4">
          {/* Tools */}
          <div className="flex items-center space-x-2">
            <span className="text-sm font-medium text-gray-700">Tool:</span>
            {tools.map((tool) => (
              <button
                key={tool.name}
                onClick={() => setSelectedTool(tool.name)}
                className={`px-3 py-2 rounded border transition-colors ${
                  selectedTool === tool.name
                    ? 'bg-primary-600 text-white border-primary-600'
                    : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                }`}
                title={tool.label}
                aria-label={tool.label}
              >
                {tool.icon}
              </button>
            ))}
          </div>

          {/* Colors */}
          <div className="flex items-center space-x-2">
            <span className="text-sm font-medium text-gray-700">Color:</span>
            {colors.map((color) => (
              <button
                key={color}
                onClick={() => setSelectedColor(color)}
                className={`w-8 h-8 rounded border-2 transition-all ${
                  selectedColor === color ? 'border-gray-900 scale-110' : 'border-gray-300'
                }`}
                style={{ backgroundColor: color }}
                aria-label={`Select ${color} color`}
              />
            ))}
          </div>

          {/* Text Input for Text Tool */}
          {selectedTool === 'text' && (
            <div className="flex items-center space-x-2">
              <input
                type="text"
                value={annotationText}
                onChange={(e) => setAnnotationText(e.target.value)}
                placeholder="Enter text..."
                className="px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>
          )}

          {/* Actions */}
          <div className="ml-auto flex items-center space-x-2">
            <button
              onClick={handleUndo}
              disabled={annotations.length === 0}
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed"
              aria-label="Undo last annotation"
            >
              Undo
            </button>
            <button
              onClick={handleClear}
              className="px-4 py-2 bg-red-100 text-red-700 rounded hover:bg-red-200"
              aria-label="Clear all annotations"
            >
              Clear All
            </button>
            <button
              onClick={handleSave}
              className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700"
              aria-label="Save annotations"
            >
              Save
            </button>
          </div>
        </div>
      </div>

      {/* Canvas */}
      <div className="bg-white rounded-lg shadow p-4">
        <div className="relative inline-block">
          <img
            ref={imageRef}
            src={imageUrl}
            alt="Annotation target"
            onLoad={handleImageLoad}
            className="max-w-full h-auto"
            style={{ display: 'none' }}
          />
          <canvas
            ref={canvasRef}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseUp}
            className="border border-gray-300 cursor-crosshair max-w-full h-auto"
          />
        </div>
      </div>

      {/* Annotation Count */}
      <div className="text-sm text-gray-600">
        Total annotations: {annotations.length}
      </div>
    </div>
  );
};

AnnotationTool.propTypes = {
  imageUrl: PropTypes.string.isRequired,
  onAnnotationSave: PropTypes.func,
  existingAnnotations: PropTypes.arrayOf(PropTypes.object),
};

export default AnnotationTool;
