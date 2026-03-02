// Input validation utilities

// Validate file size
export const validateFileSize = (file, maxSize = 10485760) => {
  // Default max size: 10MB
  if (file.size > maxSize) {
    return {
      valid: false,
      error: `File size exceeds maximum allowed size of ${maxSize / 1048576}MB`,
    };
  }
  return { valid: true };
};

// Validate file type
export const validateFileType = (file, acceptedFormats = ['.csv', '.json']) => {
  const fileName = file.name.toLowerCase();
  const isAccepted = acceptedFormats.some((format) =>
    fileName.endsWith(format.toLowerCase())
  );

  if (!isAccepted) {
    return {
      valid: false,
      error: `File type not accepted. Allowed formats: ${acceptedFormats.join(', ')}`,
    };
  }
  return { valid: true };
};

// Validate email
export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return {
      valid: false,
      error: 'Invalid email format',
    };
  }
  return { valid: true };
};

// Validate password
export const validatePassword = (password, options = {}) => {
  const {
    minLength = 8,
    requireUppercase = true,
    requireLowercase = true,
    requireNumbers = true,
    requireSpecialChars = false,
  } = options;

  const errors = [];

  if (password.length < minLength) {
    errors.push(`Password must be at least ${minLength} characters long`);
  }

  if (requireUppercase && !/[A-Z]/.test(password)) {
    errors.push('Password must contain at least one uppercase letter');
  }

  if (requireLowercase && !/[a-z]/.test(password)) {
    errors.push('Password must contain at least one lowercase letter');
  }

  if (requireNumbers && !/\d/.test(password)) {
    errors.push('Password must contain at least one number');
  }

  if (requireSpecialChars && !/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    errors.push('Password must contain at least one special character');
  }

  if (errors.length > 0) {
    return {
      valid: false,
      errors,
      error: errors.join('. '),
    };
  }

  return { valid: true };
};

// Validate MBTI type
export const validateMBTI = (mbti) => {
  const validTypes = [
    'ISTJ',
    'ISFJ',
    'INFJ',
    'INTJ',
    'ISTP',
    'ISFP',
    'INFP',
    'INTP',
    'ESTP',
    'ESFP',
    'ENFP',
    'ENTP',
    'ESTJ',
    'ESFJ',
    'ENFJ',
    'ENTJ',
  ];

  if (!validTypes.includes(mbti?.toUpperCase())) {
    return {
      valid: false,
      error: 'Invalid MBTI type',
    };
  }

  return { valid: true };
};

// Validate required fields
export const validateRequired = (value, fieldName = 'Field') => {
  if (!value || (typeof value === 'string' && !value.trim())) {
    return {
      valid: false,
      error: `${fieldName} is required`,
    };
  }
  return { valid: true };
};

// Validate number range
export const validateNumberRange = (value, min, max, fieldName = 'Value') => {
  const num = Number(value);

  if (isNaN(num)) {
    return {
      valid: false,
      error: `${fieldName} must be a number`,
    };
  }

  if (min !== undefined && num < min) {
    return {
      valid: false,
      error: `${fieldName} must be at least ${min}`,
    };
  }

  if (max !== undefined && num > max) {
    return {
      valid: false,
      error: `${fieldName} must be at most ${max}`,
    };
  }

  return { valid: true };
};

// Validate URL
export const validateURL = (url) => {
  try {
    new URL(url);
    return { valid: true };
  } catch (error) {
    return {
      valid: false,
      error: 'Invalid URL format',
    };
  }
};

// Validate date
export const validateDate = (date, options = {}) => {
  const { minDate, maxDate, format = 'ISO' } = options;

  const dateObj = new Date(date);

  if (isNaN(dateObj.getTime())) {
    return {
      valid: false,
      error: 'Invalid date format',
    };
  }

  if (minDate && dateObj < new Date(minDate)) {
    return {
      valid: false,
      error: `Date must be after ${minDate}`,
    };
  }

  if (maxDate && dateObj > new Date(maxDate)) {
    return {
      valid: false,
      error: `Date must be before ${maxDate}`,
    };
  }

  return { valid: true };
};

// Validate phone number
export const validatePhoneNumber = (phone) => {
  // Basic international phone number validation
  const phoneRegex = /^\+?[\d\s\-()]+$/;

  if (!phoneRegex.test(phone)) {
    return {
      valid: false,
      error: 'Invalid phone number format',
    };
  }

  // Remove non-digits
  const digits = phone.replace(/\D/g, '');

  if (digits.length < 10 || digits.length > 15) {
    return {
      valid: false,
      error: 'Phone number must be between 10 and 15 digits',
    };
  }

  return { valid: true };
};

// Validate JSON string
export const validateJSON = (jsonString) => {
  try {
    JSON.parse(jsonString);
    return { valid: true };
  } catch (error) {
    return {
      valid: false,
      error: 'Invalid JSON format',
    };
  }
};

// Validate CSV data
export const validateCSV = (csvString) => {
  const lines = csvString.trim().split('\n');

  if (lines.length < 2) {
    return {
      valid: false,
      error: 'CSV must have at least a header row and one data row',
    };
  }

  const headerCount = lines[0].split(',').length;

  for (let i = 1; i < lines.length; i++) {
    const columnCount = lines[i].split(',').length;
    if (columnCount !== headerCount) {
      return {
        valid: false,
        error: `Row ${i + 1} has inconsistent number of columns`,
      };
    }
  }

  return { valid: true };
};

// Sanitize input string
export const sanitizeInput = (input) => {
  if (typeof input !== 'string') return input;

  return input
    .trim()
    .replace(/[<>]/g, '') // Remove < and >
    .replace(/javascript:/gi, '') // Remove javascript: protocol
    .replace(/on\w+=/gi, ''); // Remove event handlers
};

// Validate form data
export const validateFormData = (formData, rules) => {
  const errors = {};

  Object.keys(rules).forEach((field) => {
    const fieldRules = rules[field];
    const value = formData[field];

    fieldRules.forEach((rule) => {
      if (rule.type === 'required') {
        const result = validateRequired(value, rule.fieldName || field);
        if (!result.valid) {
          errors[field] = result.error;
        }
      } else if (rule.type === 'email') {
        const result = validateEmail(value);
        if (!result.valid) {
          errors[field] = result.error;
        }
      } else if (rule.type === 'password') {
        const result = validatePassword(value, rule.options);
        if (!result.valid) {
          errors[field] = result.error;
        }
      } else if (rule.type === 'custom' && rule.validator) {
        const result = rule.validator(value);
        if (!result.valid) {
          errors[field] = result.error;
        }
      }
    });
  });

  return {
    valid: Object.keys(errors).length === 0,
    errors,
  };
};

export default {
  validateFileSize,
  validateFileType,
  validateEmail,
  validatePassword,
  validateMBTI,
  validateRequired,
  validateNumberRange,
  validateURL,
  validateDate,
  validatePhoneNumber,
  validateJSON,
  validateCSV,
  sanitizeInput,
  validateFormData,
};
