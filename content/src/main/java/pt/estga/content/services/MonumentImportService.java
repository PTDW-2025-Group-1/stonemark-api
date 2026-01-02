package pt.estga.content.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MonumentRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonumentImportService {

    private final MonumentRepository repository;

    public List<Monument> overpass(String query) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(query);
        JsonNode features = rootNode.path("features");

        Map<String, Monument> monumentMap = new LinkedHashMap<>();

        if (features.isArray()) {
            for (JsonNode feature : features) {
                JsonNode properties = feature.path("properties");
                JsonNode geometry = feature.path("geometry");

                if (properties.isObject() && geometry.isObject()) {
                    String name = properties.path("name").asText(null);
                    if (name == null || name.isBlank()) continue;

                    String description = properties.path("description").asText(null);
                    String website = properties.path("heritage:website").asText(null);
                    String protectionTitle = properties.path("protection_title").asText(null);

                    JsonNode coordinates = geometry.path("coordinates");
                    if (coordinates.isArray() && coordinates.size() >= 2) {
                        double longitude = coordinates.get(0).asDouble();
                        double latitude = coordinates.get(1).asDouble();

                        // Resolve address and city locally from OSM tags to avoid API rate limits
                        String city = resolveCity(properties);
                        String address = resolveAddress(properties);

                        // Create a transient monument object
                        Monument monumentFromJson = new Monument();
                        monumentFromJson.setName(name);
                        monumentFromJson.setDescription(description);
                        monumentFromJson.setLatitude(latitude);
                        monumentFromJson.setLongitude(longitude);
                        monumentFromJson.setWebsite(website);
                        monumentFromJson.setProtectionTitle(protectionTitle);
                        monumentFromJson.setAddress(address);
                        monumentFromJson.setCity(city);

                        monumentMap.put(name, monumentFromJson);
                    }
                }
            }
        }

        List<Monument> monumentsToSave = new ArrayList<>();
        for (Monument m : monumentMap.values()) {
            Optional<Monument> existing = repository.findByName(m.getName());
            monumentsToSave.add(getMonumentToSave(m, existing));
        }

        return repository.saveAll(monumentsToSave);
    }

    private String resolveCity(JsonNode properties) {
        if (properties.has("addr:city")) return properties.get("addr:city").asText();
        if (properties.has("addr:town")) return properties.get("addr:town").asText();
        if (properties.has("addr:village")) return properties.get("addr:village").asText();
        if (properties.has("addr:hamlet")) return properties.get("addr:hamlet").asText();
        return null;
    }

    private String resolveAddress(JsonNode properties) {
        if (properties.has("addr:full")) return properties.get("addr:full").asText();
        
        String street = properties.path("addr:street").asText(null);
        if (street != null) {
            String number = properties.path("addr:housenumber").asText(null);
            return street + (number != null ? " " + number : "");
        }
        return null;
    }

    private static Monument getMonumentToSave(Monument fromJson, Optional<Monument> existing) {
        if (existing.isPresent()) {
            Monument m = existing.get();
            m.setDescription(fromJson.getDescription());
            m.setLatitude(fromJson.getLatitude());
            m.setLongitude(fromJson.getLongitude());
            m.setWebsite(fromJson.getWebsite());
            m.setProtectionTitle(fromJson.getProtectionTitle());
            m.setAddress(fromJson.getAddress());
            m.setCity(fromJson.getCity());
            return m;
        }
        return fromJson;
    }
}
