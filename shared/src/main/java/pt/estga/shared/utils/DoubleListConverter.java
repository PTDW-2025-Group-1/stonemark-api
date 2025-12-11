package pt.estga.shared.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Converter
@Slf4j
public class DoubleListConverter implements AttributeConverter<List<Double>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting List<Double> to JSON string: {}", e.getMessage());
            return null; // Or throw an exception, depending on desired error handling
        }
    }

    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class));
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON string to List<Double>: {}", e.getMessage());
            return Collections.emptyList(); // Or throw an exception
        }
    }
}
