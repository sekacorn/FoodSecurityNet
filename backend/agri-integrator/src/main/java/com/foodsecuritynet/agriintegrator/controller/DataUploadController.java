package com.foodsecuritynet.agriintegrator.controller;

import com.foodsecuritynet.agriintegrator.model.AgriData;
import com.foodsecuritynet.agriintegrator.model.EnvData;
import com.foodsecuritynet.agriintegrator.model.SocioEconData;
import com.foodsecuritynet.agriintegrator.service.DataIntegrationService;
import com.foodsecuritynet.agriintegrator.service.DataParsingService;
import com.foodsecuritynet.agriintegrator.service.DataValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DataUploadController {

    private final DataParsingService parsingService;
    private final DataValidationService validationService;
    private final DataIntegrationService integrationService;

    @PostMapping(value = "/upload/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadCsvFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("dataType") String dataType) {

        log.info("Received CSV upload request for data type: {}", dataType);

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File is empty"));
            }

            // Parse CSV
            List<?> parsedData = parsingService.parseCsv(file, dataType);

            // Validate data
            List<String> validationErrors = validationService.validate(parsedData, dataType);

            if (!validationErrors.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "validation_failed");
                response.put("errors", validationErrors);
                response.put("recordsParsed", parsedData.size());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
            }

            // Integrate data
            int recordsSaved = integrationService.integrateData(parsedData, dataType);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Data uploaded and integrated successfully");
            response.put("recordsProcessed", parsedData.size());
            response.put("recordsSaved", recordsSaved);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error uploading CSV file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process file: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/upload/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> uploadJsonData(
            @RequestBody Map<String, Object> jsonData,
            @RequestParam("dataType") String dataType) {

        log.info("Received JSON upload request for data type: {}", dataType);

        try {
            // Parse JSON
            List<?> parsedData = parsingService.parseJson(jsonData, dataType);

            // Validate data
            List<String> validationErrors = validationService.validate(parsedData, dataType);

            if (!validationErrors.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "validation_failed");
                response.put("errors", validationErrors);
                response.put("recordsParsed", parsedData.size());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
            }

            // Integrate data
            int recordsSaved = integrationService.integrateData(parsedData, dataType);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Data uploaded and integrated successfully");
            response.put("recordsProcessed", parsedData.size());
            response.put("recordsSaved", recordsSaved);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error uploading JSON data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process data: " + e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateData(
            @RequestBody List<Map<String, Object>> data,
            @RequestParam("dataType") String dataType) {

        log.info("Validating data for type: {}", dataType);

        try {
            List<?> parsedData = parsingService.parseJson(Map.of("data", data), dataType);
            List<String> validationErrors = validationService.validate(parsedData, dataType);

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", validationErrors.isEmpty());
            response.put("errors", validationErrors);
            response.put("recordsValidated", parsedData.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validating data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Validation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDataStats() {
        log.info("Fetching data statistics");

        try {
            Map<String, Object> stats = integrationService.getDataStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics: " + e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearData(@RequestParam("dataType") String dataType) {
        log.info("Clearing data for type: {}", dataType);

        try {
            integrationService.clearData(dataType);
            return ResponseEntity.ok(Map.of("message", "Data cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to clear data: " + e.getMessage()));
        }
    }
}
