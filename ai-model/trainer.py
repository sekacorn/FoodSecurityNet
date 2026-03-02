import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset, random_split
import numpy as np
import pandas as pd
from typing import Dict, Tuple, Optional
import os
from datetime import datetime

from model import FarmingRecommendationModel, MultiTaskFarmingModel
from data_processor import FarmingDataProcessor
import config


class ModelTrainer:
    """
    Training pipeline for farming recommendation models
    """

    def __init__(
        self,
        model: nn.Module,
        device: str = 'cpu',
        learning_rate: float = 0.001,
        batch_size: int = 32
    ):
        self.model = model.to(device)
        self.device = device
        self.learning_rate = learning_rate
        self.batch_size = batch_size
        self.optimizer = optim.Adam(model.parameters(), lr=learning_rate)
        self.scheduler = optim.lr_scheduler.ReduceLROnPlateau(
            self.optimizer, mode='min', factor=0.5, patience=5, verbose=True
        )
        self.criterion = nn.CrossEntropyLoss()
        self.history = {
            'train_loss': [],
            'val_loss': [],
            'train_acc': [],
            'val_acc': []
        }

    def prepare_dataloaders(
        self,
        X: np.ndarray,
        y: np.ndarray,
        validation_split: float = 0.2
    ) -> Tuple[DataLoader, DataLoader]:
        """
        Create train and validation dataloaders

        Args:
            X: Input features
            y: Target labels
            validation_split: Fraction of data for validation

        Returns:
            Tuple of (train_loader, val_loader)
        """
        # Convert to tensors
        X_tensor = torch.FloatTensor(X)
        y_tensor = torch.LongTensor(y)

        # Create dataset
        dataset = TensorDataset(X_tensor, y_tensor)

        # Split into train and validation
        val_size = int(len(dataset) * validation_split)
        train_size = len(dataset) - val_size
        train_dataset, val_dataset = random_split(dataset, [train_size, val_size])

        # Create dataloaders
        train_loader = DataLoader(
            train_dataset,
            batch_size=self.batch_size,
            shuffle=True,
            num_workers=0
        )
        val_loader = DataLoader(
            val_dataset,
            batch_size=self.batch_size,
            shuffle=False,
            num_workers=0
        )

        return train_loader, val_loader

    def train_epoch(self, train_loader: DataLoader) -> Tuple[float, float]:
        """
        Train for one epoch

        Returns:
            Tuple of (average_loss, accuracy)
        """
        self.model.train()
        total_loss = 0
        correct = 0
        total = 0

        for batch_X, batch_y in train_loader:
            batch_X = batch_X.to(self.device)
            batch_y = batch_y.to(self.device)

            # Forward pass
            self.optimizer.zero_grad()
            outputs = self.model(batch_X)
            loss = self.criterion(outputs, batch_y)

            # Backward pass
            loss.backward()
            self.optimizer.step()

            # Track metrics
            total_loss += loss.item()
            _, predicted = torch.max(outputs.data, 1)
            total += batch_y.size(0)
            correct += (predicted == batch_y).sum().item()

        avg_loss = total_loss / len(train_loader)
        accuracy = correct / total

        return avg_loss, accuracy

    def validate(self, val_loader: DataLoader) -> Tuple[float, float]:
        """
        Validate the model

        Returns:
            Tuple of (average_loss, accuracy)
        """
        self.model.eval()
        total_loss = 0
        correct = 0
        total = 0

        with torch.no_grad():
            for batch_X, batch_y in val_loader:
                batch_X = batch_X.to(self.device)
                batch_y = batch_y.to(self.device)

                outputs = self.model(batch_X)
                loss = self.criterion(outputs, batch_y)

                total_loss += loss.item()
                _, predicted = torch.max(outputs.data, 1)
                total += batch_y.size(0)
                correct += (predicted == batch_y).sum().item()

        avg_loss = total_loss / len(val_loader)
        accuracy = correct / total

        return avg_loss, accuracy

    def train(
        self,
        train_loader: DataLoader,
        val_loader: DataLoader,
        epochs: int = 100,
        early_stopping_patience: int = 10
    ):
        """
        Train the model

        Args:
            train_loader: Training data loader
            val_loader: Validation data loader
            epochs: Number of training epochs
            early_stopping_patience: Patience for early stopping
        """
        best_val_loss = float('inf')
        patience_counter = 0

        print(f"Training on device: {self.device}")
        print(f"Total epochs: {epochs}")
        print("-" * 60)

        for epoch in range(epochs):
            # Train
            train_loss, train_acc = self.train_epoch(train_loader)

            # Validate
            val_loss, val_acc = self.validate(val_loader)

            # Update learning rate
            self.scheduler.step(val_loss)

            # Save history
            self.history['train_loss'].append(train_loss)
            self.history['val_loss'].append(val_loss)
            self.history['train_acc'].append(train_acc)
            self.history['val_acc'].append(val_acc)

            # Print progress
            print(f"Epoch {epoch+1}/{epochs}")
            print(f"  Train Loss: {train_loss:.4f} | Train Acc: {train_acc:.4f}")
            print(f"  Val Loss: {val_loss:.4f} | Val Acc: {val_acc:.4f}")
            print("-" * 60)

            # Early stopping
            if val_loss < best_val_loss:
                best_val_loss = val_loss
                patience_counter = 0
                # Save best model
                self.save_model('best_model.pt')
            else:
                patience_counter += 1
                if patience_counter >= early_stopping_patience:
                    print(f"Early stopping triggered after {epoch+1} epochs")
                    break

    def save_model(self, filepath: str):
        """Save model to disk"""
        torch.save({
            'model_state_dict': self.model.state_dict(),
            'optimizer_state_dict': self.optimizer.state_dict(),
            'history': self.history
        }, filepath)
        print(f"Model saved to {filepath}")

    def load_model(self, filepath: str):
        """Load model from disk"""
        checkpoint = torch.load(filepath, map_location=self.device)
        self.model.load_state_dict(checkpoint['model_state_dict'])
        self.optimizer.load_state_dict(checkpoint['optimizer_state_dict'])
        self.history = checkpoint.get('history', self.history)
        print(f"Model loaded from {filepath}")


def generate_synthetic_data(n_samples: int = 1000) -> Tuple[pd.DataFrame, np.ndarray]:
    """
    Generate synthetic farming data for training

    Args:
        n_samples: Number of samples to generate

    Returns:
        Tuple of (features_df, labels)
    """
    np.random.seed(42)

    data = {
        # Agricultural features
        'farm_size': np.random.uniform(0.5, 50, n_samples),
        'soil_ph': np.random.uniform(4.5, 8.5, n_samples),
        'soil_nitrogen': np.random.uniform(0, 100, n_samples),
        'soil_phosphorus': np.random.uniform(0, 100, n_samples),
        'soil_potassium': np.random.uniform(0, 100, n_samples),
        'irrigation_frequency': np.random.randint(0, 7, n_samples),

        # Environmental features
        'rainfall': np.random.uniform(200, 2000, n_samples),
        'temperature': np.random.uniform(10, 40, n_samples),
        'humidity': np.random.uniform(30, 90, n_samples),
        'elevation': np.random.uniform(0, 3000, n_samples),

        # Socioeconomic features
        'income': np.random.uniform(1000, 100000, n_samples),
        'distance_to_market': np.random.uniform(0, 100, n_samples),
        'education_level': np.random.randint(0, 5, n_samples),
        'access_to_credit': np.random.randint(0, 2, n_samples),
        'extension_services': np.random.randint(0, 2, n_samples),

        # Categorical features
        'region': np.random.choice(['north', 'south', 'east', 'west', 'central'], n_samples),
        'soil_type': np.random.choice(['clay', 'loam', 'sandy', 'silt'], n_samples),
        'current_crop': np.random.choice(config.CROP_TYPES, n_samples)
    }

    df = pd.DataFrame(data)

    # Generate labels (combined strategy index)
    # Simple rule-based labeling for demonstration
    labels = np.random.randint(0, config.OUTPUT_SIZE, n_samples)

    return df, labels


def train_pretrained_model():
    """
    Train and save a pretrained model
    """
    print("Generating synthetic training data...")
    X_df, y = generate_synthetic_data(n_samples=5000)

    print("Preprocessing data...")
    processor = FarmingDataProcessor()
    X = processor.fit_transform(X_df)

    # Save processor
    processor.save('data_processor.pkl')
    print("Data processor saved to data_processor.pkl")

    # Create model
    device = 'cuda' if torch.cuda.is_available() else 'cpu'
    print(f"Using device: {device}")

    model = FarmingRecommendationModel(
        input_size=X.shape[1],
        hidden_sizes=config.HIDDEN_SIZES,
        output_size=config.OUTPUT_SIZE,
        dropout_rate=config.DROPOUT_RATE
    )

    # Create trainer
    trainer = ModelTrainer(
        model=model,
        device=device,
        learning_rate=config.LEARNING_RATE,
        batch_size=config.BATCH_SIZE
    )

    # Prepare data
    train_loader, val_loader = trainer.prepare_dataloaders(
        X, y, validation_split=config.VALIDATION_SPLIT
    )

    # Train
    print("\nStarting training...")
    trainer.train(
        train_loader=train_loader,
        val_loader=val_loader,
        epochs=config.EPOCHS,
        early_stopping_patience=10
    )

    # Save final model
    trainer.save_model('model.pt')
    print("\nTraining complete!")


if __name__ == '__main__':
    train_pretrained_model()
