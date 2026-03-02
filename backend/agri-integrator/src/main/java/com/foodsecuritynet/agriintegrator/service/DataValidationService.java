package com.foodsecuritynet.agriintegrator.service;

import com.foodsecuritynet.agriintegrator.model.AgriData;
import com.foodsecuritynet.agriintegrator.model.EnvData;
import com.foodsecuritynet.agriintegrator.model.SocioEconData;
import com.foodsecuritynet.agriintegrator.util.InputValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataValidationService {

    private final InputValidator inputValidator;

    public List<String> validate(List<?> data, String dataType) {
        log.info("Validating {} records of type: {}", data.size(), dataType);

        List<String> errors = new ArrayList<>();

        if (data.isEmpty()) {
            errors.add("No data to validate");
            return errors;
        }

        for (int i = 0; i < data.size(); i++) {
            Object record = data.get(i);
            List<String> recordErrors = validateRecord(record, dataType, i);
            errors.addAll(recordErrors);
        }

        log.info("Validation completed. Found {} errors", errors.size());
        return errors;
    }

    private List<String> validateRecord(Object record, String dataType, int index) {
        return switch (dataType.toLowerCase()) {
            case "agri", "agriculture" -> inputValidator.validateAgriData((AgriData) record, index);
            case "env", "environment" -> inputValidator.validateEnvData((EnvData) record, index);
            case "socio", "socioeconomic" -> inputValidator.validateSocioEconData((SocioEconData) record, index);
            default -> List.of("Unknown data type: " + dataType);
        };
    }

    public boolean isValidDataType(String dataType) {
        return dataType != null && (
                dataType.equalsIgnoreCase("agri") ||
                dataType.equalsIgnoreCase("agriculture") ||
                dataType.equalsIgnoreCase("env") ||
                dataType.equalsIgnoreCase("environment") ||
                dataType.equalsIgnoreCase("socio") ||
                dataType.equalsIgnoreCase("socioeconomic")
        );
    }
}
