import React, { useRef, useState, useEffect } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, PerspectiveCamera, Grid, Text } from '@react-three/drei';
import PropTypes from 'prop-types';

// 3D Farm Plot Component
const FarmPlot = ({ data, color = '#22c55e' }) => {
  const meshRef = useRef();

  useFrame(() => {
    if (meshRef.current) {
      meshRef.current.rotation.y += 0.001;
    }
  });

  return (
    <group>
      {data.map((plot, index) => (
        <mesh
          key={index}
          ref={index === 0 ? meshRef : null}
          position={[plot.x || 0, plot.y || 0, plot.z || 0]}
        >
          <boxGeometry args={[plot.width || 1, plot.height || 1, plot.depth || 1]} />
          <meshStandardMaterial color={color} opacity={0.8} transparent />
        </mesh>
      ))}
    </group>
  );
};

FarmPlot.propTypes = {
  data: PropTypes.arrayOf(PropTypes.object).isRequired,
  color: PropTypes.string,
};

// Crop Yield Bars Component
const CropYieldBars = ({ crops }) => {
  return (
    <group>
      {crops.map((crop, index) => {
        const height = (crop.yield / 100) * 5;
        const xPos = (index - crops.length / 2) * 2;

        return (
          <group key={index} position={[xPos, height / 2, 0]}>
            <mesh>
              <boxGeometry args={[1.5, height, 1.5]} />
              <meshStandardMaterial color={crop.color || '#4ade80'} />
            </mesh>
            <Text
              position={[0, height / 2 + 0.5, 0]}
              fontSize={0.3}
              color="black"
              anchorX="center"
              anchorY="middle"
            >
              {crop.name}
            </Text>
            <Text
              position={[0, height / 2 + 1, 0]}
              fontSize={0.25}
              color="blue"
              anchorX="center"
              anchorY="middle"
            >
              {crop.yield}%
            </Text>
          </group>
        );
      })}
    </group>
  );
};

CropYieldBars.propTypes = {
  crops: PropTypes.arrayOf(
    PropTypes.shape({
      name: PropTypes.string.isRequired,
      yield: PropTypes.number.isRequired,
      color: PropTypes.string,
    })
  ).isRequired,
};

// Main AgriViewer Component
const AgriViewer = ({ data, visualizationType = 'farm', width = '100%', height = '600px' }) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (data && data.length > 0) {
      setLoading(false);
      setError(null);
    } else if (data && data.length === 0) {
      setLoading(false);
      setError('No data available to visualize');
    }
  }, [data]);

  const renderVisualization = () => {
    if (!data || data.length === 0) {
      return null;
    }

    switch (visualizationType) {
      case 'farm':
        return <FarmPlot data={data} />;
      case 'crops':
        return <CropYieldBars crops={data} />;
      default:
        return <FarmPlot data={data} />;
    }
  };

  if (loading) {
    return (
      <div
        className="flex items-center justify-center bg-gray-100 rounded-lg"
        style={{ width, height }}
      >
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading 3D visualization...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div
        className="flex items-center justify-center bg-gray-100 rounded-lg"
        style={{ width, height }}
      >
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
    <div className="relative bg-gray-900 rounded-lg overflow-hidden" style={{ width, height }}>
      <Canvas>
        <PerspectiveCamera makeDefault position={[10, 10, 10]} />
        <OrbitControls
          enableZoom={true}
          enablePan={true}
          enableRotate={true}
          zoomSpeed={0.6}
          panSpeed={0.5}
          rotateSpeed={0.4}
        />

        {/* Lighting */}
        <ambientLight intensity={0.5} />
        <directionalLight position={[10, 10, 5]} intensity={1} />
        <pointLight position={[-10, -10, -5]} intensity={0.5} />

        {/* Grid */}
        <Grid
          args={[20, 20]}
          cellSize={1}
          cellThickness={0.5}
          cellColor="#6b7280"
          sectionSize={5}
          sectionThickness={1}
          sectionColor="#374151"
          fadeDistance={30}
          fadeStrength={1}
          followCamera={false}
        />

        {/* Render visualization based on type */}
        {renderVisualization()}
      </Canvas>

      {/* Controls Info */}
      <div className="absolute bottom-4 left-4 bg-black bg-opacity-50 text-white px-4 py-2 rounded text-sm">
        <p>Left click + drag: Rotate</p>
        <p>Right click + drag: Pan</p>
        <p>Scroll: Zoom</p>
      </div>

      {/* Visualization Type Badge */}
      <div className="absolute top-4 right-4 bg-primary-600 text-white px-3 py-1 rounded-full text-sm font-medium">
        {visualizationType.charAt(0).toUpperCase() + visualizationType.slice(1)} View
      </div>
    </div>
  );
};

AgriViewer.propTypes = {
  data: PropTypes.arrayOf(PropTypes.object).isRequired,
  visualizationType: PropTypes.oneOf(['farm', 'crops']),
  width: PropTypes.string,
  height: PropTypes.string,
};

export default AgriViewer;
