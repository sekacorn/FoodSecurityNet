package com.foodsecuritynet.agrivisualizer.controller;

import com.foodsecuritynet.agrivisualizer.model.Visualization;
import com.foodsecuritynet.agrivisualizer.service.ThreeDModelService;
import com.foodsecuritynet.agrivisualizer.service.VisualizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/visualizations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VisualizationController {

    private final VisualizationService visualizationService;
    private final ThreeDModelService threeDModelService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createVisualization(
            @RequestBody Map<String, Object> request) {

        log.info("Creating visualization: {}", request.get("type"));

        try {
            String type = (String) request.get("type");
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");

            Visualization visualization = visualizationService.createVisualization(type, parameters);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", "success",
                            "message", "Visualization created successfully",
                            "visualizationId", visualization.getId(),
                            "data", visualization
                    ));

        } catch (Exception e) {
            log.error("Error creating visualization", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create visualization: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVisualization(@PathVariable Long id) {
        log.info("Fetching visualization: {}", id);

        try {
            Visualization visualization = visualizationService.getVisualization(id);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", visualization
            ));

        } catch (Exception e) {
            log.error("Error fetching visualization", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Visualization not found: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listVisualizations(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Listing visualizations - type: {}, page: {}, size: {}", type, page, size);

        try {
            List<Visualization> visualizations = visualizationService.listVisualizations(type, page, size);
            long totalCount = visualizationService.countVisualizations(type);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", visualizations,
                    "page", page,
                    "size", size,
                    "totalCount", totalCount
            ));

        } catch (Exception e) {
            log.error("Error listing visualizations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to list visualizations: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/generate-3d")
    public ResponseEntity<Map<String, Object>> generate3DModel(
            @PathVariable Long id,
            @RequestBody Map<String, Object> parameters) {

        log.info("Generating 3D model for visualization: {}", id);

        try {
            Map<String, Object> model3D = threeDModelService.generate3DModel(id, parameters);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "3D model generated successfully",
                    "model", model3D
            ));

        } catch (Exception e) {
            log.error("Error generating 3D model", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate 3D model: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteVisualization(@PathVariable Long id) {
        log.info("Deleting visualization: {}", id);

        try {
            visualizationService.deleteVisualization(id);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Visualization deleted successfully"
            ));

        } catch (Exception e) {
            log.error("Error deleting visualization", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete visualization: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getVisualizationStats() {
        log.info("Fetching visualization statistics");

        try {
            Map<String, Object> stats = visualizationService.getStatistics();

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", stats
            ));

        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics: " + e.getMessage()));
        }
    }
}
