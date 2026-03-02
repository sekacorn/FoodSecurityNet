// MBTI-based UI customization and personalization

const mbtiProfiles = {
  // Analysts
  INTJ: {
    name: 'Architect',
    borderColor: 'border-purple-600',
    textColor: 'text-purple-700',
    badgeColor: 'bg-purple-100 text-purple-800',
    iconColor: 'text-purple-600',
    benefitsLabel: 'Strategic Benefits',
    actionsLabel: 'Implementation Steps',
    presentationStyle: 'concise',
    detailLevel: 'high',
  },
  INTP: {
    name: 'Logician',
    borderColor: 'border-indigo-600',
    textColor: 'text-indigo-700',
    badgeColor: 'bg-indigo-100 text-indigo-800',
    iconColor: 'text-indigo-600',
    benefitsLabel: 'Logical Advantages',
    actionsLabel: 'Analytical Steps',
    presentationStyle: 'detailed',
    detailLevel: 'very-high',
  },
  ENTJ: {
    name: 'Commander',
    borderColor: 'border-red-600',
    textColor: 'text-red-700',
    badgeColor: 'bg-red-100 text-red-800',
    iconColor: 'text-red-600',
    benefitsLabel: 'Competitive Advantages',
    actionsLabel: 'Action Plan',
    presentationStyle: 'executive',
    detailLevel: 'medium',
  },
  ENTP: {
    name: 'Debater',
    borderColor: 'border-pink-600',
    textColor: 'text-pink-700',
    badgeColor: 'bg-pink-100 text-pink-800',
    iconColor: 'text-pink-600',
    benefitsLabel: 'Innovation Opportunities',
    actionsLabel: 'Experimental Approaches',
    presentationStyle: 'exploratory',
    detailLevel: 'medium',
  },

  // Diplomats
  INFJ: {
    name: 'Advocate',
    borderColor: 'border-teal-600',
    textColor: 'text-teal-700',
    badgeColor: 'bg-teal-100 text-teal-800',
    iconColor: 'text-teal-600',
    benefitsLabel: 'Holistic Benefits',
    actionsLabel: 'Thoughtful Actions',
    presentationStyle: 'narrative',
    detailLevel: 'high',
  },
  INFP: {
    name: 'Mediator',
    borderColor: 'border-cyan-600',
    textColor: 'text-cyan-700',
    badgeColor: 'bg-cyan-100 text-cyan-800',
    iconColor: 'text-cyan-600',
    benefitsLabel: 'Values-Aligned Benefits',
    actionsLabel: 'Meaningful Steps',
    presentationStyle: 'personal',
    detailLevel: 'medium',
  },
  ENFJ: {
    name: 'Protagonist',
    borderColor: 'border-green-600',
    textColor: 'text-green-700',
    badgeColor: 'bg-green-100 text-green-800',
    iconColor: 'text-green-600',
    benefitsLabel: 'Community Benefits',
    actionsLabel: 'Collaborative Actions',
    presentationStyle: 'inspiring',
    detailLevel: 'medium',
  },
  ENFP: {
    name: 'Campaigner',
    borderColor: 'border-lime-600',
    textColor: 'text-lime-700',
    badgeColor: 'bg-lime-100 text-lime-800',
    iconColor: 'text-lime-600',
    benefitsLabel: 'Creative Possibilities',
    actionsLabel: 'Flexible Approaches',
    presentationStyle: 'enthusiastic',
    detailLevel: 'low',
  },

  // Sentinels
  ISTJ: {
    name: 'Logistician',
    borderColor: 'border-blue-600',
    textColor: 'text-blue-700',
    badgeColor: 'bg-blue-100 text-blue-800',
    iconColor: 'text-blue-600',
    benefitsLabel: 'Practical Benefits',
    actionsLabel: 'Step-by-Step Guide',
    presentationStyle: 'systematic',
    detailLevel: 'very-high',
  },
  ISFJ: {
    name: 'Defender',
    borderColor: 'border-sky-600',
    textColor: 'text-sky-700',
    badgeColor: 'bg-sky-100 text-sky-800',
    iconColor: 'text-sky-600',
    benefitsLabel: 'Reliable Benefits',
    actionsLabel: 'Careful Steps',
    presentationStyle: 'supportive',
    detailLevel: 'high',
  },
  ESTJ: {
    name: 'Executive',
    borderColor: 'border-orange-600',
    textColor: 'text-orange-700',
    badgeColor: 'bg-orange-100 text-orange-800',
    iconColor: 'text-orange-600',
    benefitsLabel: 'Efficient Outcomes',
    actionsLabel: 'Clear Directives',
    presentationStyle: 'direct',
    detailLevel: 'medium',
  },
  ESFJ: {
    name: 'Consul',
    borderColor: 'border-amber-600',
    textColor: 'text-amber-700',
    badgeColor: 'bg-amber-100 text-amber-800',
    iconColor: 'text-amber-600',
    benefitsLabel: 'Cooperative Benefits',
    actionsLabel: 'Team-Oriented Steps',
    presentationStyle: 'social',
    detailLevel: 'medium',
  },

  // Explorers
  ISTP: {
    name: 'Virtuoso',
    borderColor: 'border-slate-600',
    textColor: 'text-slate-700',
    badgeColor: 'bg-slate-100 text-slate-800',
    iconColor: 'text-slate-600',
    benefitsLabel: 'Practical Advantages',
    actionsLabel: 'Hands-On Steps',
    presentationStyle: 'pragmatic',
    detailLevel: 'low',
  },
  ISFP: {
    name: 'Adventurer',
    borderColor: 'border-emerald-600',
    textColor: 'text-emerald-700',
    badgeColor: 'bg-emerald-100 text-emerald-800',
    iconColor: 'text-emerald-600',
    benefitsLabel: 'Aesthetic Benefits',
    actionsLabel: 'Creative Steps',
    presentationStyle: 'artistic',
    detailLevel: 'low',
  },
  ESTP: {
    name: 'Entrepreneur',
    borderColor: 'border-yellow-600',
    textColor: 'text-yellow-700',
    badgeColor: 'bg-yellow-100 text-yellow-800',
    iconColor: 'text-yellow-600',
    benefitsLabel: 'Action-Oriented Benefits',
    actionsLabel: 'Quick Wins',
    presentationStyle: 'dynamic',
    detailLevel: 'low',
  },
  ESFP: {
    name: 'Entertainer',
    borderColor: 'border-rose-600',
    textColor: 'text-rose-700',
    badgeColor: 'bg-rose-100 text-rose-800',
    iconColor: 'text-rose-600',
    benefitsLabel: 'Engaging Benefits',
    actionsLabel: 'Fun Approaches',
    presentationStyle: 'lively',
    detailLevel: 'low',
  },
};

// Get styles for a specific MBTI type
export const getMBTIStyles = (mbtiType) => {
  const type = mbtiType?.toUpperCase();
  return mbtiProfiles[type] || mbtiProfiles['ISTJ']; // Default to ISTJ
};

// Get all MBTI types
export const getAllMBTITypes = () => {
  return Object.keys(mbtiProfiles);
};

// Get MBTI profile information
export const getMBTIProfile = (mbtiType) => {
  const type = mbtiType?.toUpperCase();
  return mbtiProfiles[type] || null;
};

// Format recommendations based on MBTI preferences
export const formatRecommendationForMBTI = (recommendation, mbtiType) => {
  const profile = getMBTIStyles(mbtiType);

  let formattedRec = { ...recommendation };

  // Adjust detail level
  if (profile.detailLevel === 'low' && formattedRec.description) {
    // Summarize for types that prefer less detail
    const sentences = formattedRec.description.split('. ');
    formattedRec.description = sentences.slice(0, 2).join('. ') + '.';
  }

  // Adjust presentation style
  switch (profile.presentationStyle) {
    case 'executive':
      // Add executive summary
      formattedRec.executiveSummary = `Key action: ${formattedRec.title}`;
      break;
    case 'narrative':
      // Add storytelling elements
      formattedRec.story = `Imagine implementing ${formattedRec.title}...`;
      break;
    case 'systematic':
      // Ensure numbered steps are present
      if (!formattedRec.actions) {
        formattedRec.actions = ['Step 1: Review', 'Step 2: Plan', 'Step 3: Implement'];
      }
      break;
    default:
      break;
  }

  return formattedRec;
};

// Get color scheme for charts based on MBTI
export const getMBTIChartColors = (mbtiType) => {
  const profile = getMBTIStyles(mbtiType);

  // Extract base color from borderColor class
  const colorMap = {
    purple: ['#a855f7', '#9333ea', '#7e22ce'],
    indigo: ['#6366f1', '#4f46e5', '#4338ca'],
    red: ['#ef4444', '#dc2626', '#b91c1c'],
    pink: ['#ec4899', '#db2777', '#be185d'],
    teal: ['#14b8a6', '#0d9488', '#0f766e'],
    cyan: ['#06b6d4', '#0891b2', '#0e7490'],
    green: ['#22c55e', '#16a34a', '#15803d'],
    lime: ['#84cc16', '#65a30d', '#4d7c0f'],
    blue: ['#3b82f6', '#2563eb', '#1d4ed8'],
    sky: ['#0ea5e9', '#0284c7', '#0369a1'],
    orange: ['#f97316', '#ea580c', '#c2410c'],
    amber: ['#f59e0b', '#d97706', '#b45309'],
    slate: ['#64748b', '#475569', '#334155'],
    emerald: ['#10b981', '#059669', '#047857'],
    yellow: ['#eab308', '#ca8a04', '#a16207'],
    rose: ['#f43f5e', '#e11d48', '#be123c'],
  };

  const colorKey = profile.borderColor.split('-')[1];
  return colorMap[colorKey] || colorMap.blue;
};

export default {
  getMBTIStyles,
  getAllMBTITypes,
  getMBTIProfile,
  formatRecommendationForMBTI,
  getMBTIChartColors,
};
