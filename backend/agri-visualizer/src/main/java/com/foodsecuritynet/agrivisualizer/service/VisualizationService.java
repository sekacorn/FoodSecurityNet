package com.foodsecuritynet.agrivisualizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodsecuritynet.agrivisualizer.model.Visualization;
import com.foodsecuritynet.agrivisualizer.repository.VisualizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisualizationService {

    private final VisualizationRepository visualizationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Visualization createVisualization(String type, Map<String, Object> parameters) {
        log.info("Creating visualization of type: {}", type);

        Visualization visualization = Visualization.builder()
                .type(type)
                .title(parameters.getOrDefault("title", "Untitled Visualization").toString())
                .description(parameters.getOrDefault("description", "").toString())
                .status("PROCESSING")
                .build();

        // Process visualization based on type
        Map<String, Object> visualizationData = generateVisualizationData(type, parameters);
        visualization.setData(visualizationData);
        visualization.setStatus("COMPLETED");

        return visualizationRepository.save(visualization);
    }

    public Visualization getVisualization(Long id) {
        return visualizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visualization not found with id: " + id));
    }

    public List<Visualization> listVisualizations(String type, int page, int size) {
        if (type != null && !type.isEmpty()) {
            return visualizationRepository.findByType(type, PageRequest.of(page, size));
        }
        return visualizationRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    public long countVisualizations(String type) {
        if (type != null && !type.isEmpty()) {
            return visualizationRepository.countByType(type);
        }
        return visualizationRepository.count();
    }

    @Transactional
    public void deleteVisualization(Long id) {
        visualizationRepository.deleteById(id);
        log.info("Deleted visualization: {}", id);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisualizations", visualizationRepository.count());
        stats.put("visualizationsByType", visualizationRepository.countByTypeGrouped());
        return stats;
    }

    public String exportAsJson(Long id) throws Exception {
        Visualization visualization = getVisualization(id);
        return objectMapper.writeValueAsString(visualization);
    }

    public String exportAsCsv(Long id) {
        Visualization visualization = getVisualization(id);
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Type,Title,Status,Created At\n");
        csv.append(String.format("%d,%s,%s,%s,%s\n",
                visualization.getId(),
                visualization.getType(),
                visualization.getTitle(),
                visualization.getStatus(),
                visualization.getCreatedAt()));
        return csv.toString();
    }

    public String exportAsGeoJson(Long id) throws Exception {
        Visualization visualization = getVisualization(id);
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");
        geoJson.put("features", visualization.getData());
        return objectMapper.writeValueAsString(geoJson);
    }

    public byte[] exportAsPng(Long id, int width, int height) {
        // Placeholder for image generation logic
        log.info("Generating PNG export for visualization: {} ({}x{})", id, width, height);
        return new byte[0];
    }

    private Map<String, Object> generateVisualizationData(String type, Map<String, Object> parameters) {
        Map<String, Object> data = new HashMap<>();

        switch (type.toLowerCase()) {
            case "heatmap":
                data.put("type", "heatmap");
                data.put("coordinates", parameters.get("coordinates"));
                data.put("values", parameters.get("values"));
                break;

            case "choropleth":
                data.put("type", "choropleth");
                data.put("regions", parameters.get("regions"));
                data.put("values", parameters.get("values"));
                break;

            case "timeseries":
                data.put("type", "timeseries");
                data.put("timestamps", parameters.get("timestamps"));
                data.put("values", parameters.get("values"));
                break;

            case "scatter":
                data.put("type", "scatter");
                data.put("xValues", parameters.get("xValues"));
                data.put("yValues", parameters.get("yValues"));
                break;

            default:
                data.put("type", "generic");
                data.putAll(parameters);
        }

        data.put("generatedAt", LocalDateTime.now());
        return data;
    }
}
