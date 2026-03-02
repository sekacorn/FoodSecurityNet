import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler, LabelEncoder, OneHotEncoder
from sklearn.impute import SimpleImputer
from typing import Dict, List, Tuple, Optional
import pickle
import os


class FarmingDataProcessor:
    """
    Data preprocessing pipeline for farming prediction data

    Handles:
    - Feature scaling
    - Categorical encoding
    - Missing value imputation
    - Feature engineering
    """

    def __init__(self):
        self.scaler = StandardScaler()
        self.label_encoders = {}
        self.one_hot_encoders = {}
        self.imputer_numeric = SimpleImputer(strategy='median')
        self.imputer_categorical = SimpleImputer(strategy='most_frequent')
        self.feature_names = []
        self.categorical_features = []
        self.numerical_features = []
        self.is_fitted = False

    def identify_feature_types(self, df: pd.DataFrame) -> Tuple[List[str], List[str]]:
        """
        Identify categorical and numerical features

        Args:
            df: Input dataframe

        Returns:
            Tuple of (numerical_features, categorical_features)
        """
        numerical = df.select_dtypes(include=['int64', 'float64']).columns.tolist()
        categorical = df.select_dtypes(include=['object', 'category']).columns.tolist()

        return numerical, categorical

    def engineer_features(self, df: pd.DataFrame) -> pd.DataFrame:
        """
        Create engineered features from raw data

        Args:
            df: Input dataframe

        Returns:
            DataFrame with additional engineered features
        """
        df = df.copy()

        # Agricultural features
        if 'farm_size' in df.columns and 'income' in df.columns:
            df['income_per_hectare'] = df['income'] / (df['farm_size'] + 1e-6)

        if 'rainfall' in df.columns and 'temperature' in df.columns:
            df['moisture_index'] = df['rainfall'] / (df['temperature'] + 1e-6)

        # Soil health index
        soil_features = ['soil_ph', 'soil_nitrogen', 'soil_phosphorus', 'soil_potassium']
        available_soil = [f for f in soil_features if f in df.columns]
        if len(available_soil) > 0:
            df['soil_health_index'] = df[available_soil].mean(axis=1)

        # Water availability
        if 'rainfall' in df.columns and 'irrigation_frequency' in df.columns:
            df['water_availability'] = df['rainfall'] + df['irrigation_frequency'] * 10

        # Market accessibility score
        if 'distance_to_market' in df.columns:
            df['market_access_score'] = 1 / (df['distance_to_market'] + 1)

        # Productivity potential
        if 'farm_size' in df.columns and 'soil_health_index' in df.columns:
            df['productivity_potential'] = df['farm_size'] * df['soil_health_index']

        # Climate stress index
        if 'temperature' in df.columns and 'rainfall' in df.columns:
            # Higher temperature and lower rainfall = higher stress
            df['climate_stress'] = df['temperature'] / (df['rainfall'] + 1)

        return df

    def handle_missing_values(
        self,
        df: pd.DataFrame,
        numerical_features: List[str],
        categorical_features: List[str],
        fit: bool = False
    ) -> pd.DataFrame:
        """
        Impute missing values

        Args:
            df: Input dataframe
            numerical_features: List of numerical feature names
            categorical_features: List of categorical feature names
            fit: Whether to fit the imputers

        Returns:
            DataFrame with imputed values
        """
        df = df.copy()

        if len(numerical_features) > 0:
            if fit:
                df[numerical_features] = self.imputer_numeric.fit_transform(df[numerical_features])
            else:
                df[numerical_features] = self.imputer_numeric.transform(df[numerical_features])

        if len(categorical_features) > 0:
            if fit:
                df[categorical_features] = self.imputer_categorical.fit_transform(df[categorical_features])
            else:
                df[categorical_features] = self.imputer_categorical.transform(df[categorical_features])

        return df

    def encode_categorical(
        self,
        df: pd.DataFrame,
        categorical_features: List[str],
        fit: bool = False
    ) -> pd.DataFrame:
        """
        Encode categorical variables using label encoding

        Args:
            df: Input dataframe
            categorical_features: List of categorical feature names
            fit: Whether to fit the encoders

        Returns:
            DataFrame with encoded categorical features
        """
        df = df.copy()

        for feature in categorical_features:
            if feature not in df.columns:
                continue

            if fit:
                le = LabelEncoder()
                df[feature] = le.fit_transform(df[feature].astype(str))
                self.label_encoders[feature] = le
            else:
                if feature in self.label_encoders:
                    le = self.label_encoders[feature]
                    # Handle unknown categories
                    df[feature] = df[feature].astype(str).apply(
                        lambda x: x if x in le.classes_ else le.classes_[0]
                    )
                    df[feature] = le.transform(df[feature])

        return df

    def scale_features(
        self,
        df: pd.DataFrame,
        numerical_features: List[str],
        fit: bool = False
    ) -> pd.DataFrame:
        """
        Scale numerical features using StandardScaler

        Args:
            df: Input dataframe
            numerical_features: List of numerical feature names
            fit: Whether to fit the scaler

        Returns:
            DataFrame with scaled numerical features
        """
        df = df.copy()

        if len(numerical_features) > 0:
            if fit:
                df[numerical_features] = self.scaler.fit_transform(df[numerical_features])
            else:
                df[numerical_features] = self.scaler.transform(df[numerical_features])

        return df

    def fit_transform(self, df: pd.DataFrame) -> np.ndarray:
        """
        Fit the processor and transform the data

        Args:
            df: Input dataframe

        Returns:
            Transformed numpy array
        """
        # Engineer features
        df = self.engineer_features(df)

        # Identify feature types
        self.numerical_features, self.categorical_features = self.identify_feature_types(df)

        # Handle missing values
        df = self.handle_missing_values(df, self.numerical_features, self.categorical_features, fit=True)

        # Encode categorical features
        df = self.encode_categorical(df, self.categorical_features, fit=True)

        # Now all features are numerical, update the list
        self.numerical_features = df.columns.tolist()
        self.categorical_features = []

        # Scale features
        df = self.scale_features(df, self.numerical_features, fit=True)

        self.feature_names = df.columns.tolist()
        self.is_fitted = True

        return df.values

    def transform(self, df: pd.DataFrame) -> np.ndarray:
        """
        Transform the data using fitted processor

        Args:
            df: Input dataframe

        Returns:
            Transformed numpy array
        """
        if not self.is_fitted:
            raise ValueError("Processor must be fitted before transform. Use fit_transform first.")

        # Engineer features
        df = self.engineer_features(df)

        # Ensure all expected features are present
        for feature in self.feature_names:
            if feature not in df.columns:
                df[feature] = 0  # Add missing feature with default value

        # Select only the features used during training
        df = df[self.feature_names]

        # Handle missing values
        num_features = [f for f in self.numerical_features if f in df.columns]
        cat_features = [f for f in self.categorical_features if f in df.columns]

        df = self.handle_missing_values(df, num_features, cat_features, fit=False)

        # Encode categorical features
        df = self.encode_categorical(df, cat_features, fit=False)

        # Scale features
        df = self.scale_features(df, self.numerical_features, fit=False)

        return df.values

    def save(self, filepath: str):
        """Save the processor to disk"""
        with open(filepath, 'wb') as f:
            pickle.dump({
                'scaler': self.scaler,
                'label_encoders': self.label_encoders,
                'one_hot_encoders': self.one_hot_encoders,
                'imputer_numeric': self.imputer_numeric,
                'imputer_categorical': self.imputer_categorical,
                'feature_names': self.feature_names,
                'categorical_features': self.categorical_features,
                'numerical_features': self.numerical_features,
                'is_fitted': self.is_fitted
            }, f)

    @classmethod
    def load(cls, filepath: str):
        """Load the processor from disk"""
        processor = cls()
        with open(filepath, 'rb') as f:
            data = pickle.load(f)
            processor.scaler = data['scaler']
            processor.label_encoders = data['label_encoders']
            processor.one_hot_encoders = data['one_hot_encoders']
            processor.imputer_numeric = data['imputer_numeric']
            processor.imputer_categorical = data['imputer_categorical']
            processor.feature_names = data['feature_names']
            processor.categorical_features = data['categorical_features']
            processor.numerical_features = data['numerical_features']
            processor.is_fitted = data['is_fitted']
        return processor
