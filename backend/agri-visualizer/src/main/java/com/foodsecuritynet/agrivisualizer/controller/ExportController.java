package com.foodsecuritynet.agrivisualizer.controller;

import com.foodsecuritynet.agrivisualizer.service.VisualizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExportController {

    private final VisualizationService visualizationService;

    @GetMapping("/{id}/json")
    public ResponseEntity<?> exportAsJson(@PathVariable Long id) {
        log.info("Exporting visualization {} as JSON", id);

        try {
            String jsonData = visualizationService.exportAsJson(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "visualization_" + id + ".json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(jsonData);

        } catch (Exception e) {
            log.error("Error exporting visualization as JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to export: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/csv")
    public ResponseEntity<?> exportAsCsv(@PathVariable Long id) {
        log.info("Exporting visualization {} as CSV", id);

        try {
            String csvData = visualizationService.exportAsCsv(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "visualization_" + id + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);

        } catch (Exception e) {
            log.error("Error exporting visualization as CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to export: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/geojson")
    public ResponseEntity<?> exportAsGeoJson(@PathVariable Long id) {
        log.info("Exporting visualization {} as GeoJSON", id);

        try {
            String geoJsonData = visualizationService.exportAsGeoJson(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "visualization_" + id + ".geojson");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(geoJsonData);

        } catch (Exception e) {
            log.error("Error exporting visualization as GeoJSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to export: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/png")
    public ResponseEntity<?> exportAsPng(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1920") int width,
            @RequestParam(defaultValue = "1080") int height) {

        log.info("Exporting visualization {} as PNG ({}x{})", id, width, height);

        try {
            byte[] imageData = visualizationService.exportAsPng(id, width, height);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "visualization_" + id + ".png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData);

        } catch (Exception e) {
            log.error("Error exporting visualization as PNG", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to export: " + e.getMessage()));
        }
    }
}
