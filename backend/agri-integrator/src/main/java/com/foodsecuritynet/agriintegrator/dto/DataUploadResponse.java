package com.foodsecuritynet.agriintegrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for data upload operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataUploadResponse {

    private String uploadId;
    private String status;
    private String message;
    private Integer recordsProcessed;
    private Integer recordsSuccessful;
    private Integer recordsFailed;
    private LocalDateTime uploadTimestamp;
    private ValidationResult validationResult;
    private List<String> errors;
    private List<String> warnings;
    private String dataType;
    private Long fileSize;
    private String fileName;

    /**
     * Create success response
     */
    public static DataUploadResponse success(String uploadId, Integer recordsProcessed, String dataType) {
        return DataUploadResponse.builder()
                .uploadId(uploadId)
                .status("SUCCESS")
                .message("Data uploaded successfully")
                .recordsProcessed(recordsProcessed)
                .recordsSuccessful(recordsProcessed)
                .recordsFailed(0)
                .uploadTimestamp(LocalDateTime.now())
                .dataType(dataType)
                .build();
    }

    /**
     * Create partial success response
     */
    public static DataUploadResponse partialSuccess(String uploadId, Integer successful, Integer failed,
                                                     List<String> errors, String dataType) {
        return DataUploadResponse.builder()
                .uploadId(uploadId)
                .status("PARTIAL_SUCCESS")
                .message("Data uploaded with some errors")
                .recordsProcessed(successful + failed)
                .recordsSuccessful(successful)
                .recordsFailed(failed)
                .uploadTimestamp(LocalDateTime.now())
                .errors(errors)
                .dataType(dataType)
                .build();
    }

    /**
     * Create failure response
     */
    public static DataUploadResponse failure(String message, List<String> errors, String dataType) {
        return DataUploadResponse.builder()
                .status("FAILED")
                .message(message)
                .recordsProcessed(0)
                .recordsSuccessful(0)
                .recordsFailed(0)
                .uploadTimestamp(LocalDateTime.now())
                .errors(errors)
                .dataType(dataType)
                .build();
    }
}
