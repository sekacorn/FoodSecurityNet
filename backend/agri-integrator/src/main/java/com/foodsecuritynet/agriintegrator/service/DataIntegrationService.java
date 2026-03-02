package com.foodsecuritynet.agriintegrator.service;

import com.foodsecuritynet.agriintegrator.model.AgriData;
import com.foodsecuritynet.agriintegrator.model.EnvData;
import com.foodsecuritynet.agriintegrator.model.SocioEconData;
import com.foodsecuritynet.agriintegrator.repository.AgriDataRepository;
import com.foodsecuritynet.agriintegrator.repository.EnvDataRepository;
import com.foodsecuritynet.agriintegrator.repository.SocioEconDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataIntegrationService {

    private final AgriDataRepository agriDataRepository;
    private final EnvDataRepository envDataRepository;
    private final SocioEconDataRepository socioEconDataRepository;

    @Transactional
    public int integrateData(List<?> data, String dataType) {
        log.info("Integrating {} records of type: {}", data.size(), dataType);

        return switch (dataType.toLowerCase()) {
            case "agri", "agriculture" -> {
                List<AgriData> agriData = (List<AgriData>) data;
                agriDataRepository.saveAll(agriData);
                yield agriData.size();
            }
            case "env", "environment" -> {
                List<EnvData> envData = (List<EnvData>) data;
                envDataRepository.saveAll(envData);
                yield envData.size();
            }
            case "socio", "socioeconomic" -> {
                List<SocioEconData> socioData = (List<SocioEconData>) data;
                socioEconDataRepository.saveAll(socioData);
                yield socioData.size();
            }
            default -> throw new IllegalArgumentException("Unknown data type: " + dataType);
        };
    }

    public Map<String, Object> getDataStatistics() {
        log.info("Fetching data statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("agriDataCount", agriDataRepository.count());
        stats.put("envDataCount", envDataRepository.count());
        stats.put("socioEconDataCount", socioEconDataRepository.count());
        stats.put("totalRecords",
                (Long) stats.get("agriDataCount") +
                (Long) stats.get("envDataCount") +
                (Long) stats.get("socioEconDataCount"));

        return stats;
    }

    @Transactional
    public void clearData(String dataType) {
        log.info("Clearing data for type: {}", dataType);

        switch (dataType.toLowerCase()) {
            case "agri", "agriculture" -> agriDataRepository.deleteAll();
            case "env", "environment" -> envDataRepository.deleteAll();
            case "socio", "socioeconomic" -> socioEconDataRepository.deleteAll();
            case "all" -> {
                agriDataRepository.deleteAll();
                envDataRepository.deleteAll();
                socioEconDataRepository.deleteAll();
            }
            default -> throw new IllegalArgumentException("Unknown data type: " + dataType);
        }
    }
}
