"""
Model management module for loading and managing AI models
"""
from typing import Dict, Any, Optional
import os
import logging
from transformers import AutoTokenizer, AutoModelForCausalLM, AutoModelForSeq2SeqLM
import torch
import requests

from config import (
    DEFAULT_MODEL, FALLBACK_MODEL, MODEL_CACHE_DIR,
    XAI_API_KEY, XAI_API_URL, USE_XAI, OPENAI_API_KEY,
    MAX_LENGTH, TEMPERATURE
)

logger = logging.getLogger(__name__)


class ModelManager:
    """
    Manages AI models for text generation and query processing
    """

    def __init__(self):
        self.models = {}
        self.tokenizers = {}
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        logger.info(f"Using device: {self.device}")

        self.use_xai = USE_XAI
        self.xai_api_key = XAI_API_KEY
        self.openai_api_key = OPENAI_API_KEY

        # Model configurations
        self.model_configs = {
            'gpt2': {
                'type': 'causal',
                'model_name': 'gpt2',
                'max_length': 512
            },
            'gpt2-medium': {
                'type': 'causal',
                'model_name': 'gpt2-medium',
                'max_length': 1024
            },
            'flan-t5-base': {
                'type': 'seq2seq',
                'model_name': 'google/flan-t5-base',
                'max_length': 512
            },
            'flan-t5-large': {
                'type': 'seq2seq',
                'model_name': 'google/flan-t5-large',
                'max_length': 512
            }
        }

    def load_model(self, model_name: str = None) -> bool:
        """
        Load a model into memory

        Args:
            model_name: Name of the model to load

        Returns:
            True if successful, False otherwise
        """
        if model_name is None:
            model_name = DEFAULT_MODEL

        if model_name in self.models:
            logger.info(f"Model {model_name} already loaded")
            return True

        try:
            config = self.model_configs.get(model_name)
            if not config:
                logger.error(f"Unknown model: {model_name}")
                return False

            logger.info(f"Loading model: {model_name}")

            # Load tokenizer
            self.tokenizers[model_name] = AutoTokenizer.from_pretrained(
                config['model_name'],
                cache_dir=MODEL_CACHE_DIR
            )

            # Load model based on type
            if config['type'] == 'causal':
                self.models[model_name] = AutoModelForCausalLM.from_pretrained(
                    config['model_name'],
                    cache_dir=MODEL_CACHE_DIR
                ).to(self.device)
            elif config['type'] == 'seq2seq':
                self.models[model_name] = AutoModelForSeq2SeqLM.from_pretrained(
                    config['model_name'],
                    cache_dir=MODEL_CACHE_DIR
                ).to(self.device)

            logger.info(f"Successfully loaded model: {model_name}")
            return True

        except Exception as e:
            logger.error(f"Error loading model {model_name}: {str(e)}")
            return False

    def generate_response(self, prompt: str, model_name: str = None,
                         max_length: int = None, temperature: float = None) -> str:
        """
        Generate a response using the specified model

        Args:
            prompt: Input prompt
            model_name: Model to use (defaults to DEFAULT_MODEL)
            max_length: Maximum response length
            temperature: Sampling temperature

        Returns:
            Generated response string
        """
        # Use API if configured
        if self.use_xai and self.xai_api_key:
            return self._generate_with_xai(prompt, max_length, temperature)

        # Use local model
        if model_name is None:
            model_name = DEFAULT_MODEL

        if model_name not in self.models:
            if not self.load_model(model_name):
                # Try fallback model
                logger.warning(f"Falling back to {FALLBACK_MODEL}")
                model_name = FALLBACK_MODEL
                if not self.load_model(model_name):
                    return "Error: Unable to load any model"

        try:
            config = self.model_configs[model_name]
            tokenizer = self.tokenizers[model_name]
            model = self.models[model_name]

            if max_length is None:
                max_length = min(MAX_LENGTH, config['max_length'])

            if temperature is None:
                temperature = TEMPERATURE

            # Tokenize input
            inputs = tokenizer(prompt, return_tensors="pt", truncation=True,
                             max_length=max_length).to(self.device)

            # Generate
            with torch.no_grad():
                if config['type'] == 'causal':
                    outputs = model.generate(
                        **inputs,
                        max_length=max_length,
                        temperature=temperature,
                        do_sample=True,
                        top_p=0.9,
                        pad_token_id=tokenizer.eos_token_id
                    )
                    response = tokenizer.decode(outputs[0], skip_special_tokens=True)
                    # Remove the prompt from response for causal models
                    response = response[len(prompt):].strip()

                else:  # seq2seq
                    outputs = model.generate(
                        **inputs,
                        max_length=max_length,
                        temperature=temperature,
                        do_sample=True,
                        top_p=0.9
                    )
                    response = tokenizer.decode(outputs[0], skip_special_tokens=True)

            return response

        except Exception as e:
            logger.error(f"Error generating response: {str(e)}")
            return f"Error generating response: {str(e)}"

    def _generate_with_xai(self, prompt: str, max_length: int = None,
                          temperature: float = None) -> str:
        """
        Generate response using xAI API

        Args:
            prompt: Input prompt
            max_length: Maximum response length
            temperature: Sampling temperature

        Returns:
            Generated response string
        """
        try:
            headers = {
                "Authorization": f"Bearer {self.xai_api_key}",
                "Content-Type": "application/json"
            }

            data = {
                "model": "grok-1",
                "messages": [
                    {"role": "system", "content": "You are an expert agricultural advisor."},
                    {"role": "user", "content": prompt}
                ],
                "max_tokens": max_length or MAX_LENGTH,
                "temperature": temperature or TEMPERATURE
            }

            response = requests.post(
                f"{XAI_API_URL}/chat/completions",
                headers=headers,
                json=data,
                timeout=30
            )

            if response.status_code == 200:
                result = response.json()
                return result['choices'][0]['message']['content']
            else:
                logger.error(f"xAI API error: {response.status_code} - {response.text}")
                return f"API Error: {response.status_code}"

        except Exception as e:
            logger.error(f"Error calling xAI API: {str(e)}")
            return f"Error calling API: {str(e)}"

    def _generate_with_openai(self, prompt: str, max_length: int = None,
                             temperature: float = None) -> str:
        """
        Generate response using OpenAI API (fallback)

        Args:
            prompt: Input prompt
            max_length: Maximum response length
            temperature: Sampling temperature

        Returns:
            Generated response string
        """
        try:
            import openai
            openai.api_key = self.openai_api_key

            response = openai.ChatCompletion.create(
                model="gpt-3.5-turbo",
                messages=[
                    {"role": "system", "content": "You are an expert agricultural advisor."},
                    {"role": "user", "content": prompt}
                ],
                max_tokens=max_length or MAX_LENGTH,
                temperature=temperature or TEMPERATURE
            )

            return response.choices[0].message.content

        except Exception as e:
            logger.error(f"Error calling OpenAI API: {str(e)}")
            return f"Error calling OpenAI API: {str(e)}"

    def unload_model(self, model_name: str) -> bool:
        """
        Unload a model from memory

        Args:
            model_name: Name of model to unload

        Returns:
            True if successful
        """
        try:
            if model_name in self.models:
                del self.models[model_name]
                del self.tokenizers[model_name]
                torch.cuda.empty_cache()
                logger.info(f"Unloaded model: {model_name}")
            return True
        except Exception as e:
            logger.error(f"Error unloading model: {str(e)}")
            return False

    def get_loaded_models(self) -> list:
        """
        Get list of currently loaded models

        Returns:
            List of model names
        """
        return list(self.models.keys())
