import torch
import torch.nn as nn
import torch.nn.functional as F
from typing import List


class FarmingRecommendationModel(nn.Module):
    """
    Neural Network for Farming Recommendations

    Predicts optimal farming strategies based on:
    - Agricultural data (crop type, soil metrics, irrigation)
    - Environmental data (climate, rainfall, temperature)
    - Socioeconomic data (income, market access, farm size)

    Outputs recommendation scores for:
    - Crop selection
    - Irrigation methods
    - Fertilization strategies
    """

    def __init__(
        self,
        input_size: int,
        hidden_sizes: List[int],
        output_size: int,
        dropout_rate: float = 0.3
    ):
        super(FarmingRecommendationModel, self).__init__()

        self.input_size = input_size
        self.output_size = output_size

        # Build network layers
        layers = []
        prev_size = input_size

        for i, hidden_size in enumerate(hidden_sizes):
            # Fully connected layer
            layers.append(nn.Linear(prev_size, hidden_size))
            layers.append(nn.BatchNorm1d(hidden_size))
            layers.append(nn.ReLU())
            layers.append(nn.Dropout(dropout_rate))
            prev_size = hidden_size

        # Output layer
        layers.append(nn.Linear(prev_size, output_size))

        self.network = nn.Sequential(*layers)

        # Initialize weights
        self._initialize_weights()

    def _initialize_weights(self):
        """Initialize network weights using He initialization"""
        for m in self.modules():
            if isinstance(m, nn.Linear):
                nn.init.kaiming_normal_(m.weight, mode='fan_out', nonlinearity='relu')
                if m.bias is not None:
                    nn.init.constant_(m.bias, 0)
            elif isinstance(m, nn.BatchNorm1d):
                nn.init.constant_(m.weight, 1)
                nn.init.constant_(m.bias, 0)

    def forward(self, x):
        """
        Forward pass through the network

        Args:
            x: Input tensor of shape (batch_size, input_size)

        Returns:
            Output tensor of shape (batch_size, output_size)
        """
        return self.network(x)

    def predict_probabilities(self, x):
        """
        Get probability distributions for predictions

        Args:
            x: Input tensor

        Returns:
            Softmax probabilities
        """
        logits = self.forward(x)
        return F.softmax(logits, dim=1)

    def get_top_k_recommendations(self, x, k=3):
        """
        Get top K recommendations

        Args:
            x: Input tensor
            k: Number of top recommendations to return

        Returns:
            Tuple of (values, indices) for top K recommendations
        """
        with torch.no_grad():
            probs = self.predict_probabilities(x)
            top_k = torch.topk(probs, k, dim=1)
        return top_k


class MultiTaskFarmingModel(nn.Module):
    """
    Multi-task learning model for separate prediction heads
    for crop selection, irrigation, and fertilization
    """

    def __init__(
        self,
        input_size: int,
        shared_hidden_sizes: List[int],
        crop_output_size: int,
        irrigation_output_size: int,
        fertilization_output_size: int,
        dropout_rate: float = 0.3
    ):
        super(MultiTaskFarmingModel, self).__init__()

        # Shared feature extraction layers
        shared_layers = []
        prev_size = input_size

        for hidden_size in shared_hidden_sizes:
            shared_layers.append(nn.Linear(prev_size, hidden_size))
            shared_layers.append(nn.BatchNorm1d(hidden_size))
            shared_layers.append(nn.ReLU())
            shared_layers.append(nn.Dropout(dropout_rate))
            prev_size = hidden_size

        self.shared_network = nn.Sequential(*shared_layers)

        # Task-specific heads
        self.crop_head = nn.Sequential(
            nn.Linear(prev_size, 64),
            nn.ReLU(),
            nn.Dropout(dropout_rate),
            nn.Linear(64, crop_output_size)
        )

        self.irrigation_head = nn.Sequential(
            nn.Linear(prev_size, 64),
            nn.ReLU(),
            nn.Dropout(dropout_rate),
            nn.Linear(64, irrigation_output_size)
        )

        self.fertilization_head = nn.Sequential(
            nn.Linear(prev_size, 64),
            nn.ReLU(),
            nn.Dropout(dropout_rate),
            nn.Linear(64, fertilization_output_size)
        )

        self._initialize_weights()

    def _initialize_weights(self):
        """Initialize network weights"""
        for m in self.modules():
            if isinstance(m, nn.Linear):
                nn.init.kaiming_normal_(m.weight, mode='fan_out', nonlinearity='relu')
                if m.bias is not None:
                    nn.init.constant_(m.bias, 0)
            elif isinstance(m, nn.BatchNorm1d):
                nn.init.constant_(m.weight, 1)
                nn.init.constant_(m.bias, 0)

    def forward(self, x):
        """
        Forward pass through shared network and all task heads

        Returns:
            Dictionary with predictions for each task
        """
        shared_features = self.shared_network(x)

        return {
            'crop': self.crop_head(shared_features),
            'irrigation': self.irrigation_head(shared_features),
            'fertilization': self.fertilization_head(shared_features)
        }

    def predict_probabilities(self, x):
        """Get probability distributions for all tasks"""
        outputs = self.forward(x)
        return {
            'crop': F.softmax(outputs['crop'], dim=1),
            'irrigation': F.softmax(outputs['irrigation'], dim=1),
            'fertilization': F.softmax(outputs['fertilization'], dim=1)
        }
