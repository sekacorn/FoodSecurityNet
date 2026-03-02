import React, { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';
import { validateFileSize, validateFileType } from '../utils/validators';

const DataUpload = ({ onUploadSuccess, acceptedFormats = ['.csv', '.json'], maxSize = 10485760 }) => {
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [uploadedFile, setUploadedFile] = useState(null);

  const onDrop = useCallback(
    async (acceptedFiles, rejectedFiles) => {
      if (rejectedFiles.length > 0) {
        const errors = rejectedFiles[0].errors;
        errors.forEach((error) => {
          if (error.code === 'file-too-large') {
            toast.error(`File is too large. Max size is ${maxSize / 1048576}MB`);
          } else if (error.code === 'file-invalid-type') {
            toast.error(`Invalid file type. Accepted: ${acceptedFormats.join(', ')}`);
          } else {
            toast.error(error.message);
          }
        });
        return;
      }

      if (acceptedFiles.length === 0) return;

      const file = acceptedFiles[0];

      // Validate file
      const sizeValidation = validateFileSize(file, maxSize);
      if (!sizeValidation.valid) {
        toast.error(sizeValidation.error);
        return;
      }

      const typeValidation = validateFileType(file, acceptedFormats);
      if (!typeValidation.valid) {
        toast.error(typeValidation.error);
        return;
      }

      setUploading(true);
      setProgress(0);

      try {
        // Simulate upload progress
        const progressInterval = setInterval(() => {
          setProgress((prev) => {
            if (prev >= 90) {
              clearInterval(progressInterval);
              return 90;
            }
            return prev + 10;
          });
        }, 200);

        // Read file content
        const fileContent = await readFileContent(file);

        clearInterval(progressInterval);
        setProgress(100);

        setUploadedFile({
          name: file.name,
          size: file.size,
          type: file.type,
          content: fileContent,
        });

        toast.success(`File "${file.name}" uploaded successfully!`);

        if (onUploadSuccess) {
          onUploadSuccess({
            file,
            content: fileContent,
          });
        }
      } catch (error) {
        console.error('Upload error:', error);
        toast.error(`Upload failed: ${error.message}`);
      } finally {
        setUploading(false);
        setTimeout(() => setProgress(0), 1000);
      }
    },
    [acceptedFormats, maxSize, onUploadSuccess]
  );

  const readFileContent = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = (e) => {
        try {
          const content = e.target.result;
          if (file.name.endsWith('.json')) {
            resolve(JSON.parse(content));
          } else {
            resolve(content);
          }
        } catch (error) {
          reject(new Error('Failed to parse file content'));
        }
      };

      reader.onerror = () => reject(new Error('Failed to read file'));
      reader.readAsText(file);
    });
  };

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: acceptedFormats.reduce((acc, format) => {
      if (format === '.csv') acc['text/csv'] = ['.csv'];
      if (format === '.json') acc['application/json'] = ['.json'];
      return acc;
    }, {}),
    maxSize,
    multiple: false,
  });

  const handleClear = () => {
    setUploadedFile(null);
    setProgress(0);
  };

  return (
    <div className="w-full">
      <div
        {...getRootProps()}
        className={`
          border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-all
          ${isDragActive ? 'border-primary-500 bg-primary-50' : 'border-gray-300 hover:border-primary-400'}
          ${uploading ? 'pointer-events-none opacity-50' : ''}
        `}
        role="button"
        tabIndex={0}
        aria-label="File upload area"
      >
        <input {...getInputProps()} aria-label="File input" />

        <div className="flex flex-col items-center space-y-4">
          <svg
            className="w-16 h-16 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
            />
          </svg>

          {isDragActive ? (
            <p className="text-lg font-medium text-primary-600">Drop the file here...</p>
          ) : (
            <>
              <p className="text-lg font-medium text-gray-700">
                Drag and drop your file here, or click to select
              </p>
              <p className="text-sm text-gray-500">
                Accepted formats: {acceptedFormats.join(', ')} (max {maxSize / 1048576}MB)
              </p>
            </>
          )}
        </div>
      </div>

      {uploading && (
        <div className="mt-4" role="progressbar" aria-valuenow={progress} aria-valuemin="0" aria-valuemax="100">
          <div className="flex justify-between mb-1">
            <span className="text-sm font-medium text-gray-700">Uploading...</span>
            <span className="text-sm font-medium text-gray-700">{progress}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2.5">
            <div
              className="bg-primary-600 h-2.5 rounded-full transition-all duration-300"
              style={{ width: `${progress}%` }}
            ></div>
          </div>
        </div>
      )}

      {uploadedFile && !uploading && (
        <div className="mt-4 p-4 bg-green-50 border border-green-200 rounded-lg">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <svg
                className="w-8 h-8 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <div>
                <p className="font-medium text-gray-900">{uploadedFile.name}</p>
                <p className="text-sm text-gray-600">
                  {(uploadedFile.size / 1024).toFixed(2)} KB
                </p>
              </div>
            </div>
            <button
              onClick={handleClear}
              className="text-red-600 hover:text-red-800 font-medium"
              aria-label="Clear uploaded file"
            >
              Clear
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

DataUpload.propTypes = {
  onUploadSuccess: PropTypes.func,
  acceptedFormats: PropTypes.arrayOf(PropTypes.string),
  maxSize: PropTypes.number,
};

export default DataUpload;
