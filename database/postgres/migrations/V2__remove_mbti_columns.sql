-- Migration: Remove MBTI columns
-- MBTI personality typing is no longer part of the product.

ALTER TABLE users DROP COLUMN IF EXISTS mbti_type;
ALTER TABLE farming_recommendations DROP COLUMN IF EXISTS mbti_tailored;
ALTER TABLE llm_queries DROP COLUMN IF EXISTS mbti_type;
