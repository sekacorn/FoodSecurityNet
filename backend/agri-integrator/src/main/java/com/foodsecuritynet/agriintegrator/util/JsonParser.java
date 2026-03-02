package com.foodsecuritynet.agriintegrator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodsecuritynet.agriintegrator.model.AgriData;
import com.foodsecuritynet.agriintegrator.model.EnvData;
import com.foodsecuritynet.agriintegrator.model.SocioEconData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JsonParser {

    private final ObjectMapper objectMapper;

    public List<AgriData> parseAgriData(Map<String, Object> jsonData) throws Exception {
        List<AgriData> dataList = new ArrayList<>();

        Object data = jsonData.get("data");
        if (data instanceof List) {
            List<Map<String, Object>> records = (List<Map<String, Object>>) data;
            for (Map<String, Object> record : records) {
                AgriData agriData = objectMapper.convertValue(record, AgriData.class);
                dataList.add(agriData);
            }
        } else if (data instanceof Map) {
            AgriData agriData = objectMapper.convertValue(data, AgriData.class);
            dataList.add(agriData);
        }

        log.info("Parsed {} agriculture data records from JSON", dataList.size());
        return dataList;
    }

    public List<EnvData> parseEnvData(Map<String, Object> jsonData) throws Exception {
        List<EnvData> dataList = new ArrayList<>();

        Object data = jsonData.get("data");
        if (data instanceof List) {
            List<Map<String, Object>> records = (List<Map<String, Object>>) data;
            for (Map<String, Object> record : records) {
                EnvData envData = objectMapper.convertValue(record, EnvData.class);
                dataList.add(envData);
            }
        } else if (data instanceof Map) {
            EnvData envData = objectMapper.convertValue(data, EnvData.class);
            dataList.add(envData);
        }

        log.info("Parsed {} environment data records from JSON", dataList.size());
        return dataList;
    }

    public List<SocioEconData> parseSocioEconData(Map<String, Object> jsonData) throws Exception {
        List<SocioEconData> dataList = new ArrayList<>();

        Object data = jsonData.get("data");
        if (data instanceof List) {
            List<Map<String, Object>> records = (List<Map<String, Object>>) data;
            for (Map<String, Object> record : records) {
                SocioEconData socioEconData = objectMapper.convertValue(record, SocioEconData.class);
                dataList.add(socioEconData);
            }
        } else if (data instanceof Map) {
            SocioEconData socioEconData = objectMapper.convertValue(data, SocioEconData.class);
            dataList.add(socioEconData);
        }

        log.info("Parsed {} socioeconomic data records from JSON", dataList.size());
        return dataList;
    }
}
