package com.foodsecuritynet.agriintegrator.util;

import com.foodsecuritynet.agriintegrator.model.AgriData;
import com.foodsecuritynet.agriintegrator.model.EnvData;
import com.foodsecuritynet.agriintegrator.model.SocioEconData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CsvParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public List<AgriData> parseAgriData(MultipartFile file) throws Exception {
        List<AgriData> dataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                AgriData data = AgriData.builder()
                        .region(record.get("region"))
                        .cropType(record.get("cropType"))
                        .cropVariety(getOptionalValue(record, "cropVariety"))
                        .yield(parseDouble(record.get("yield")))
                        .yieldUnit(getOptionalValue(record, "yieldUnit"))
                        .plantedArea(parseDoubleOptional(record, "plantedArea"))
                        .areaUnit(getOptionalValue(record, "areaUnit"))
                        .harvestDate(parseDateTime(getOptionalValue(record, "harvestDate")))
                        .plantingDate(parseDateTime(getOptionalValue(record, "plantingDate")))
                        .irrigationType(getOptionalValue(record, "irrigationType"))
                        .fertilizerUsed(getOptionalValue(record, "fertilizerUsed"))
                        .pesticideUsed(getOptionalValue(record, "pesticideUsed"))
                        .latitude(parseDoubleOptional(record, "latitude"))
                        .longitude(parseDoubleOptional(record, "longitude"))
                        .soilType(getOptionalValue(record, "soilType"))
                        .farmingMethod(getOptionalValue(record, "farmingMethod"))
                        .notes(getOptionalValue(record, "notes"))
                        .build();

                dataList.add(data);
            }
        }

        log.info("Parsed {} agriculture data records from CSV", dataList.size());
        return dataList;
    }

    public List<EnvData> parseEnvData(MultipartFile file) throws Exception {
        List<EnvData> dataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                EnvData data = EnvData.builder()
                        .region(record.get("region"))
                        .measurementDate(parseDateTime(record.get("measurementDate")))
                        .temperatureAvg(parseDoubleOptional(record, "temperatureAvg"))
                        .temperatureMin(parseDoubleOptional(record, "temperatureMin"))
                        .temperatureMax(parseDoubleOptional(record, "temperatureMax"))
                        .temperatureUnit(getOptionalValue(record, "temperatureUnit"))
                        .rainfall(parseDoubleOptional(record, "rainfall"))
                        .rainfallUnit(getOptionalValue(record, "rainfallUnit"))
                        .humidity(parseDoubleOptional(record, "humidity"))
                        .humidityUnit(getOptionalValue(record, "humidityUnit"))
                        .windSpeed(parseDoubleOptional(record, "windSpeed"))
                        .windSpeedUnit(getOptionalValue(record, "windSpeedUnit"))
                        .soilMoisture(parseDoubleOptional(record, "soilMoisture"))
                        .soilMoistureUnit(getOptionalValue(record, "soilMoistureUnit"))
                        .soilPh(parseDoubleOptional(record, "soilPh"))
                        .solarRadiation(parseDoubleOptional(record, "solarRadiation"))
                        .solarRadiationUnit(getOptionalValue(record, "solarRadiationUnit"))
                        .latitude(parseDoubleOptional(record, "latitude"))
                        .longitude(parseDoubleOptional(record, "longitude"))
                        .weatherCondition(getOptionalValue(record, "weatherCondition"))
                        .airQualityIndex(parseIntegerOptional(record, "airQualityIndex"))
                        .notes(getOptionalValue(record, "notes"))
                        .build();

                dataList.add(data);
            }
        }

        log.info("Parsed {} environment data records from CSV", dataList.size());
        return dataList;
    }

    public List<SocioEconData> parseSocioEconData(MultipartFile file) throws Exception {
        List<SocioEconData> dataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                SocioEconData data = SocioEconData.builder()
                        .region(record.get("region"))
                        .dataDate(parseDateTime(record.get("dataDate")))
                        .population(parseLongOptional(record, "population"))
                        .farmingHouseholds(parseIntegerOptional(record, "farmingHouseholds"))
                        .avgHouseholdIncome(parseDoubleOptional(record, "avgHouseholdIncome"))
                        .incomeCurrency(getOptionalValue(record, "incomeCurrency"))
                        .povertyRate(parseDoubleOptional(record, "povertyRate"))
                        .unemploymentRate(parseDoubleOptional(record, "unemploymentRate"))
                        .literacyRate(parseDoubleOptional(record, "literacyRate"))
                        .marketAccess(getOptionalValue(record, "marketAccess"))
                        .avgLandSize(parseDoubleOptional(record, "avgLandSize"))
                        .landSizeUnit(getOptionalValue(record, "landSizeUnit"))
                        .foodSecurityIndex(parseDoubleOptional(record, "foodSecurityIndex"))
                        .malnutritionRate(parseDoubleOptional(record, "malnutritionRate"))
                        .agriculturalEmploymentRate(parseDoubleOptional(record, "agriculturalEmploymentRate"))
                        .infrastructureScore(parseIntegerOptional(record, "infrastructureScore"))
                        .technologyAdoptionRate(parseDoubleOptional(record, "technologyAdoptionRate"))
                        .latitude(parseDoubleOptional(record, "latitude"))
                        .longitude(parseDoubleOptional(record, "longitude"))
                        .notes(getOptionalValue(record, "notes"))
                        .build();

                dataList.add(data);
            }
        }

        log.info("Parsed {} socioeconomic data records from CSV", dataList.size());
        return dataList;
    }

    private String getOptionalValue(CSVRecord record, String columnName) {
        try {
            String value = record.get(columnName);
            return (value == null || value.trim().isEmpty()) ? null : value.trim();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Double.parseDouble(value.trim());
    }

    private Double parseDoubleOptional(CSVRecord record, String columnName) {
        String value = getOptionalValue(record, columnName);
        return value != null ? Double.parseDouble(value) : null;
    }

    private Integer parseIntegerOptional(CSVRecord record, String columnName) {
        String value = getOptionalValue(record, columnName);
        return value != null ? Integer.parseInt(value) : null;
    }

    private Long parseLongOptional(CSVRecord record, String columnName) {
        String value = getOptionalValue(record, columnName);
        return value != null ? Long.parseLong(value) : null;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", value);
            return null;
        }
    }
}
