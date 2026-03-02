"""
Query processing module for intent detection and context extraction
"""
from typing import Dict, Any, List, Tuple
import re


class QueryProcessor:
    """
    Processes natural language queries for farming and food security
    """

    def __init__(self):
        self.intent_keywords = {
            'farming_advice': [
                'plant', 'grow', 'crop', 'soil', 'fertilizer', 'harvest',
                'irrigation', 'pest', 'disease', 'seed', 'farming', 'agriculture',
                'cultivation', 'planting', 'watering', 'yield'
            ],
            'troubleshooting': [
                'problem', 'issue', 'error', 'wrong', 'failing', 'not working',
                'help', 'fix', 'broken', 'trouble', 'dying', 'wilting', 'yellow'
            ],
            'visualization': [
                'show', 'display', 'visualize', 'chart', 'graph', 'plot',
                'data', 'statistics', 'trends', 'analysis', 'dashboard'
            ],
            'weather': [
                'weather', 'rain', 'temperature', 'forecast', 'climate',
                'drought', 'humidity', 'wind', 'storm'
            ],
            'market': [
                'price', 'sell', 'market', 'cost', 'profit', 'demand',
                'trade', 'buyer', 'value'
            ],
            'general': []  # catch-all
        }

        self.context_patterns = {
            'crop_type': r'\b(maize|wheat|rice|beans|tomato|potato|cassava|millet|sorghum|vegetables?)\b',
            'location': r'\b([A-Z][a-z]+(?:\s+[A-Z][a-z]+)*),?\s*(?:Kenya|Uganda|Tanzania|Rwanda)?\b',
            'timeframe': r'\b(today|tomorrow|week|month|season|year|daily|weekly|monthly)\b',
            'quantity': r'\b(\d+\.?\d*)\s*(kg|ton|hectare|acre|meter|liter)s?\b',
            'symptoms': r'\b(yellow|wilting|spots|brown|dry|wet|infected|damaged)\b'
        }

    def process_query(self, query: str, user_context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        Process a natural language query

        Args:
            query: Natural language query string
            user_context: Optional user context (location, preferences, etc.)

        Returns:
            Dictionary containing intent, entities, and context
        """
        query_lower = query.lower()

        # Detect intent
        intent = self._detect_intent(query_lower)

        # Extract entities and context
        entities = self._extract_entities(query)

        # Build context
        context = {
            'intent': intent,
            'entities': entities,
            'original_query': query,
            'user_context': user_context or {}
        }

        # Add query classification
        context['query_type'] = self._classify_query_type(intent, entities)

        return context

    def _detect_intent(self, query: str) -> str:
        """
        Detect the primary intent of the query

        Args:
            query: Lowercased query string

        Returns:
            Intent string
        """
        intent_scores = {}

        for intent, keywords in self.intent_keywords.items():
            if intent == 'general':
                continue

            score = sum(1 for keyword in keywords if keyword in query)
            if score > 0:
                intent_scores[intent] = score

        if not intent_scores:
            return 'general'

        # Return intent with highest score
        return max(intent_scores.items(), key=lambda x: x[1])[0]

    def _extract_entities(self, query: str) -> Dict[str, List[str]]:
        """
        Extract entities from query using regex patterns

        Args:
            query: Query string

        Returns:
            Dictionary of entity types and their values
        """
        entities = {}

        for entity_type, pattern in self.context_patterns.items():
            matches = re.findall(pattern, query, re.IGNORECASE)
            if matches:
                entities[entity_type] = matches if isinstance(matches[0], str) else [m[0] for m in matches]

        return entities

    def _classify_query_type(self, intent: str, entities: Dict[str, List[str]]) -> str:
        """
        Classify the type of query for response formatting

        Args:
            intent: Detected intent
            entities: Extracted entities

        Returns:
            Query type string
        """
        if intent == 'troubleshooting' and 'symptoms' in entities:
            return 'diagnostic'
        elif intent == 'farming_advice' and 'crop_type' in entities:
            return 'crop_specific'
        elif intent == 'weather':
            return 'weather_forecast'
        elif intent == 'market':
            return 'market_info'
        elif intent == 'visualization':
            return 'data_visualization'
        else:
            return 'general_inquiry'

    def format_response(self, response: str, context: Dict[str, Any]) -> Dict[str, Any]:
        """
        Format the response based on query context

        Args:
            response: Raw response text
            context: Query context

        Returns:
            Formatted response dictionary
        """
        query_type = context.get('query_type', 'general_inquiry')

        formatted = {
            'response': response,
            'query_type': query_type,
            'intent': context.get('intent'),
            'entities': context.get('entities', {}),
            'metadata': {}
        }

        # Add type-specific formatting
        if query_type == 'diagnostic':
            formatted['metadata']['type'] = 'diagnostic'
            formatted['metadata']['requires_followup'] = True

        elif query_type == 'crop_specific':
            crop = context.get('entities', {}).get('crop_type', ['unknown'])[0]
            formatted['metadata']['crop'] = crop
            formatted['metadata']['type'] = 'farming_advice'

        elif query_type == 'weather_forecast':
            formatted['metadata']['type'] = 'weather'
            formatted['metadata']['requires_location'] = 'location' not in context.get('entities', {})

        elif query_type == 'market_info':
            formatted['metadata']['type'] = 'market'
            formatted['metadata']['requires_data_source'] = True

        elif query_type == 'data_visualization':
            formatted['metadata']['type'] = 'visualization'
            formatted['metadata']['visualization_needed'] = True

        return formatted

    def enhance_prompt(self, query: str, context: Dict[str, Any]) -> str:
        """
        Enhance the query with context for better model responses

        Args:
            query: Original query
            context: Query context

        Returns:
            Enhanced prompt string
        """
        intent = context.get('intent', 'general')
        entities = context.get('entities', {})
        user_context = context.get('user_context', {})

        # Build enhanced prompt
        prompt_parts = []

        # Add domain context
        prompt_parts.append("You are an expert agricultural advisor for smallholder farmers in East Africa.")

        # Add intent-specific context
        if intent == 'farming_advice':
            prompt_parts.append("Provide practical, actionable farming advice.")
        elif intent == 'troubleshooting':
            prompt_parts.append("Diagnose the problem and provide solutions.")
        elif intent == 'weather':
            prompt_parts.append("Provide weather-related guidance for farming decisions.")
        elif intent == 'market':
            prompt_parts.append("Provide market insights and pricing guidance.")

        # Add entity context
        if entities:
            entity_context = []
            if 'crop_type' in entities:
                entity_context.append(f"Crop: {', '.join(entities['crop_type'])}")
            if 'location' in entities:
                entity_context.append(f"Location: {', '.join(entities['location'])}")
            if 'timeframe' in entities:
                entity_context.append(f"Timeframe: {', '.join(entities['timeframe'])}")

            if entity_context:
                prompt_parts.append(f"Context: {'; '.join(entity_context)}")

        # Add user context
        if user_context:
            if 'location' in user_context:
                prompt_parts.append(f"User location: {user_context['location']}")
            if 'farm_size' in user_context:
                prompt_parts.append(f"Farm size: {user_context['farm_size']}")

        # Add the actual query
        prompt_parts.append(f"\nQuery: {query}")
        prompt_parts.append("\nProvide a clear, practical response:")

        return "\n".join(prompt_parts)
