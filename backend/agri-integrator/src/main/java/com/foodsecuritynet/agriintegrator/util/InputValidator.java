package com.foodsecuritynet.agriintegrator.util;

import com.foodsecuritynet.agriintegrator.model.AgriData;
import com.foodsecuritynet.agriintegrator.model.EnvData;
import com.foodsecuritynet.agriintegrator.model.SocioEconData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class InputValidator {

    public List<String> validateAgriData(AgriData data, int index) {
        List<String> errors = new ArrayList<>();

        if (data.getRegion() == null || data.getRegion().trim().isEmpty()) {
            errors.add(String.format("Record %d: Region is required", index));
        }

        if (data.getCropType() == null || data.getCropType().trim().isEmpty()) {
            errors.add(String.format("Record %d: Crop type is required", index));
        }

        if (data.getYield() == null || data.getYield() < 0) {
            errors.add(String.format("Record %d: Yield must be a positive number", index));
        }

        if (data.getLatitude() != null && (data.getLatitude() < -90 || data.getLatitude() > 90)) {
            errors.add(String.format("Record %d: Latitude must be between -90 and 90", index));
        }

        if (data.getLongitude() != null && (data.getLongitude() < -180 || data.getLongitude() > 180)) {
            errors.add(String.format("Record %d: Longitude must be between -180 and 180", index));
        }

        if (data.getPlantedArea() != null && data.getPlantedArea() < 0) {
            errors.add(String.format("Record %d: Planted area must be a positive number", index));
        }

        return errors;
    }

    public List<String> validateEnvData(EnvData data, int index) {
        List<String> errors = new ArrayList<>();

        if (data.getRegion() == null || data.getRegion().trim().isEmpty()) {
            errors.add(String.format("Record %d: Region is required", index));
        }

        if (data.getMeasurementDate() == null) {
            errors.add(String.format("Record %d: Measurement date is required", index));
        }

        if (data.getLatitude() != null && (data.getLatitude() < -90 || data.getLatitude() > 90)) {
            errors.add(String.format("Record %d: Latitude must be between -90 and 90", index));
        }

        if (data.getLongitude() != null && (data.getLongitude() < -180 || data.getLongitude() > 180)) {
            errors.add(String.format("Record %d: Longitude must be between -180 and 180", index));
        }

        if (data.getHumidity() != null && (data.getHumidity() < 0 || data.getHumidity() > 100)) {
            errors.add(String.format("Record %d: Humidity must be between 0 and 100", index));
        }

        if (data.getSoilPh() != null && (data.getSoilPh() < 0 || data.getSoilPh() > 14)) {
            errors.add(String.format("Record %d: Soil pH must be between 0 and 14", index));
        }

        if (data.getRainfall() != null && data.getRainfall() < 0) {
            errors.add(String.format("Record %d: Rainfall must be a positive number", index));
        }

        return errors;
    }

    public List<String> validateSocioEconData(SocioEconData data, int index) {
        List<String> errors = new ArrayList<>();

        if (data.getRegion() == null || data.getRegion().trim().isEmpty()) {
            errors.add(String.format("Record %d: Region is required", index));
        }

        if (data.getDataDate() == null) {
            errors.add(String.format("Record %d: Data date is required", index));
        }

        if (data.getLatitude() != null && (data.getLatitude() < -90 || data.getLatitude() > 90)) {
            errors.add(String.format("Record %d: Latitude must be between -90 and 90", index));
        }

        if (data.getLongitude() != null && (data.getLongitude() < -180 || data.getLongitude() > 180)) {
            errors.add(String.format("Record %d: Longitude must be between -180 and 180", index));
        }

        if (data.getPopulation() != null && data.getPopulation() < 0) {
            errors.add(String.format("Record %d: Population must be a positive number", index));
        }

        if (data.getPovertyRate() != null && (data.getPovertyRate() < 0 || data.getPovertyRate() > 100)) {
            errors.add(String.format("Record %d: Poverty rate must be between 0 and 100", index));
        }

        if (data.getUnemploymentRate() != null && (data.getUnemploymentRate() < 0 || data.getUnemploymentRate() > 100)) {
            errors.add(String.format("Record %d: Unemployment rate must be between 0 and 100", index));
        }

        if (data.getLiteracyRate() != null && (data.getLiteracyRate() < 0 || data.getLiteracyRate() > 100)) {
            errors.add(String.format("Record %d: Literacy rate must be between 0 and 100", index));
        }

        return errors;
    }
}
