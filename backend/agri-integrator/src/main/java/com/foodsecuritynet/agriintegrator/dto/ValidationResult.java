package com.foodsecuritynet.agriintegrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for validation results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    private Boolean isValid;
    private Integer totalRecords;
    private Integer validRecords;
    private Integer invalidRecords;

    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();

    @Builder.Default
    private List<ValidationWarning> warnings = new ArrayList<>();

    private String validationTimestamp;

    /**
     * Nested class for validation errors
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private Integer recordNumber;
        private String fieldName;
        private String errorMessage;
        private String errorCode;
        private String providedValue;
    }

    /**
     * Nested class for validation warnings
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationWarning {
        private Integer recordNumber;
        private String fieldName;
        private String warningMessage;
        private String providedValue;
        private String suggestedValue;
    }

    /**
     * Add validation error
     */
    public void addError(Integer recordNumber, String fieldName, String errorMessage, String errorCode, String providedValue) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(ValidationError.builder()
                .recordNumber(recordNumber)
                .fieldName(fieldName)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .providedValue(providedValue)
                .build());
        this.invalidRecords = this.errors.size();
    }

    /**
     * Add validation warning
     */
    public void addWarning(Integer recordNumber, String fieldName, String warningMessage, String providedValue, String suggestedValue) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(ValidationWarning.builder()
                .recordNumber(recordNumber)
                .fieldName(fieldName)
                .warningMessage(warningMessage)
                .providedValue(providedValue)
                .suggestedValue(suggestedValue)
                .build());
    }

    /**
     * Check if validation has errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Check if validation has warnings
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Create success validation result
     */
    public static ValidationResult success(Integer totalRecords) {
        return ValidationResult.builder()
                .isValid(true)
                .totalRecords(totalRecords)
                .validRecords(totalRecords)
                .invalidRecords(0)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();
    }

    /**
     * Create failed validation result
     */
    public static ValidationResult failed(Integer totalRecords, List<ValidationError> errors) {
        return ValidationResult.builder()
                .isValid(false)
                .totalRecords(totalRecords)
                .validRecords(totalRecords - errors.size())
                .invalidRecords(errors.size())
                .errors(errors)
                .warnings(new ArrayList<>())
                .build();
    }
}
