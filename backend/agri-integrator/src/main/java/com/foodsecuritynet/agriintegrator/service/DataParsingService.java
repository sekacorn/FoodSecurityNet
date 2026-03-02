package com.foodsecuritynet.agriintegrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodsecuritynet.agriintegrator.model.AgriData;
import com.foodsecuritynet.agriintegrator.model.EnvData;
import com.foodsecuritynet.agriintegrator.model.SocioEconData;
import com.foodsecuritynet.agriintegrator.util.CsvParser;
import com.foodsecuritynet.agriintegrator.util.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataParsingService {

    private final CsvParser csvParser;
    private final JsonParser jsonParser;
    private final ObjectMapper objectMapper;

    public List<?> parseCsv(MultipartFile file, String dataType) throws Exception {
        log.info("Parsing CSV file for data type: {}", dataType);

        return switch (dataType.toLowerCase()) {
            case "agri", "agriculture" -> csvParser.parseAgriData(file);
            case "env", "environment" -> csvParser.parseEnvData(file);
            case "socio", "socioeconomic" -> csvParser.parseSocioEconData(file);
            default -> throw new IllegalArgumentException("Unknown data type: " + dataType);
        };
    }

    public List<?> parseJson(Map<String, Object> jsonData, String dataType) throws Exception {
        log.info("Parsing JSON data for data type: {}", dataType);

        return switch (dataType.toLowerCase()) {
            case "agri", "agriculture" -> jsonParser.parseAgriData(jsonData);
            case "env", "environment" -> jsonParser.parseEnvData(jsonData);
            case "socio", "socioeconomic" -> jsonParser.parseSocioEconData(jsonData);
            default -> throw new IllegalArgumentException("Unknown data type: " + dataType);
        };
    }
}
