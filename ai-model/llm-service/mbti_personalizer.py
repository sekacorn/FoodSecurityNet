"""
MBTI Personalization module for tailoring responses to different personality types
"""
from typing import Dict, Any


class MBTIPersonalizer:
    """
    Personalizes responses based on MBTI personality types
    """

    def __init__(self):
        self.mbti_profiles = {
            # Analysts
            'INTJ': {
                'tone': 'strategic and analytical',
                'style': 'structured with long-term implications',
                'focus': 'efficiency, innovation, and systematic approaches',
                'format': 'logical frameworks and data-driven insights'
            },
            'INTP': {
                'tone': 'logical and theoretical',
                'style': 'detailed explanations with underlying principles',
                'focus': 'understanding the why behind methods',
                'format': 'conceptual models and logical reasoning'
            },
            'ENTJ': {
                'tone': 'commanding and results-focused',
                'style': 'strategic with clear action steps',
                'focus': 'efficiency, leadership, and goal achievement',
                'format': 'executive summaries with actionable plans'
            },
            'ENTP': {
                'tone': 'innovative and exploratory',
                'style': 'dynamic with multiple possibilities',
                'focus': 'creative solutions and alternatives',
                'format': 'brainstorming ideas with pros and cons'
            },

            # Diplomats
            'INFJ': {
                'tone': 'empathetic and insightful',
                'style': 'holistic with personal meaning',
                'focus': 'impact on people and environment',
                'format': 'narrative with values and purpose'
            },
            'INFP': {
                'tone': 'creative and value-driven',
                'style': 'authentic with emotional resonance',
                'focus': 'alignment with personal values and ideals',
                'format': 'inspiring stories and meaningful connections'
            },
            'ENFJ': {
                'tone': 'supportive and encouraging',
                'style': 'collaborative with community focus',
                'focus': 'helping others and collective growth',
                'format': 'motivational guidance with social impact'
            },
            'ENFP': {
                'tone': 'enthusiastic and imaginative',
                'style': 'energetic with possibilities',
                'focus': 'innovation and personal growth',
                'format': 'exciting opportunities with creative angles'
            },

            # Sentinels
            'ISTJ': {
                'tone': 'practical and methodical',
                'style': 'step-by-step with proven methods',
                'focus': 'reliability, tradition, and accuracy',
                'format': 'detailed procedures and checklists'
            },
            'ISFJ': {
                'tone': 'caring and conscientious',
                'style': 'supportive with attention to detail',
                'focus': 'helping others and maintaining stability',
                'format': 'gentle guidance with practical care'
            },
            'ESTJ': {
                'tone': 'direct and organized',
                'style': 'efficient with clear standards',
                'focus': 'order, rules, and practical results',
                'format': 'structured plans with clear expectations'
            },
            'ESFJ': {
                'tone': 'warm and cooperative',
                'style': 'helpful with social harmony',
                'focus': 'community welfare and tradition',
                'format': 'friendly advice with proven practices'
            },

            # Explorers
            'ISTP': {
                'tone': 'pragmatic and hands-on',
                'style': 'technical with practical application',
                'focus': 'how things work and immediate solutions',
                'format': 'direct instructions with technical details'
            },
            'ISFP': {
                'tone': 'gentle and experiential',
                'style': 'sensory-rich with aesthetic awareness',
                'focus': 'harmony with nature and personal expression',
                'format': 'vivid descriptions with hands-on tips'
            },
            'ESTP': {
                'tone': 'bold and actionable',
                'style': 'concise with immediate tactics',
                'focus': 'quick results and practical action',
                'format': 'bullet points with immediate steps'
            },
            'ESFP': {
                'tone': 'energetic and engaging',
                'style': 'lively with practical fun',
                'focus': 'enjoyment and immediate experience',
                'format': 'entertaining guidance with hands-on activities'
            }
        }

    def tailor_response(self, response: str, mbti_type: str, query_context: Dict[str, Any]) -> str:
        """
        Tailor a response based on MBTI type

        Args:
            response: Original response text
            mbti_type: MBTI personality type (e.g., 'ENTJ')
            query_context: Context about the query

        Returns:
            Tailored response string
        """
        mbti_type = mbti_type.upper()

        if mbti_type not in self.mbti_profiles:
            return response

        profile = self.mbti_profiles[mbti_type]

        # Add MBTI-specific framing
        tailored = self._add_mbti_framing(response, profile, mbti_type)

        return tailored

    def _add_mbti_framing(self, response: str, profile: Dict[str, str], mbti_type: str) -> str:
        """
        Add MBTI-specific framing to the response
        """
        # Prefix based on MBTI category
        prefix = self._get_prefix(mbti_type, profile)

        # Suffix based on MBTI category
        suffix = self._get_suffix(mbti_type, profile)

        # Combine with appropriate formatting
        if prefix:
            response = f"{prefix}\n\n{response}"

        if suffix:
            response = f"{response}\n\n{suffix}"

        return response

    def _get_prefix(self, mbti_type: str, profile: Dict[str, str]) -> str:
        """
        Get appropriate prefix based on MBTI type
        """
        # Analysts (NT)
        if mbti_type in ['INTJ', 'INTP', 'ENTJ', 'ENTP']:
            return "Based on strategic analysis:"

        # Diplomats (NF)
        elif mbti_type in ['INFJ', 'INFP', 'ENFJ', 'ENFP']:
            return "Here's a meaningful approach:"

        # Sentinels (SJ)
        elif mbti_type in ['ISTJ', 'ISFJ', 'ESTJ', 'ESFJ']:
            return "Following proven methods:"

        # Explorers (SP)
        elif mbti_type in ['ISTP', 'ISFP', 'ESTP', 'ESFP']:
            return "Here's what you can do right now:"

        return ""

    def _get_suffix(self, mbti_type: str, profile: Dict[str, str]) -> str:
        """
        Get appropriate suffix based on MBTI type
        """
        suffixes = {
            # Analysts
            'INTJ': "This approach optimizes long-term outcomes and system efficiency.",
            'INTP': "Consider exploring the underlying principles for deeper understanding.",
            'ENTJ': "Execute these steps decisively to achieve your objectives.",
            'ENTP': "Feel free to innovate and adapt these ideas to your unique situation.",

            # Diplomats
            'INFJ': "Remember, this approach supports both environmental and human wellbeing.",
            'INFP': "Stay true to your values while implementing these practices.",
            'ENFJ': "Share these insights with your community to create collective impact.",
            'ENFP': "Get excited about the possibilities and trust your creative instincts.",

            # Sentinels
            'ISTJ': "Follow these proven steps carefully for reliable results.",
            'ISFJ': "Take care with each step to ensure the best outcome for all.",
            'ESTJ': "Implement this systematic plan to maintain high standards.",
            'ESFJ': "This approach has helped many in your community succeed.",

            # Explorers
            'ISTP': "Test this out and adjust based on what works in practice.",
            'ISFP': "Connect with the process and let your hands guide you.",
            'ESTP': "Take action immediately and adapt as you go.",
            'ESFP': "Enjoy the process and make it fun while getting results."
        }

        return suffixes.get(mbti_type, "")

    def get_query_style_prompt(self, mbti_type: str) -> str:
        """
        Get prompt modifier for query processing based on MBTI

        Args:
            mbti_type: MBTI personality type

        Returns:
            Prompt modifier string
        """
        mbti_type = mbti_type.upper()

        if mbti_type not in self.mbti_profiles:
            return ""

        profile = self.mbti_profiles[mbti_type]

        prompt = f"""
Tailor your response to an {mbti_type} personality:
- Tone: {profile['tone']}
- Style: {profile['style']}
- Focus: {profile['focus']}
- Format: {profile['format']}
"""
        return prompt.strip()

    def get_mbti_profile(self, mbti_type: str) -> Dict[str, str]:
        """
        Get the full profile for an MBTI type

        Args:
            mbti_type: MBTI personality type

        Returns:
            Dictionary containing profile information
        """
        return self.mbti_profiles.get(mbti_type.upper(), {})
