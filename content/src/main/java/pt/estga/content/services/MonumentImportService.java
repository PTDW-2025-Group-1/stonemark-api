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

        // Use a map to handle duplicates within the JSON data itself.
        // The last entry with a given name will overwrite previous ones.
        Map<String, Monument> monumentMap = new LinkedHashMap<>();

        if (features.isArray()) {
            for (JsonNode feature : features) {
                JsonNode properties = feature.path("properties");
                JsonNode geometry = feature.path("geometry");

                if (properties.isObject() && geometry.isObject()) {
                    String name = properties.path("name").asText(null);
                    if (name == null || name.isBlank()) {
                        continue; // Skip entries without a name
                    }

                    String description = properties.path("description").asText(null);
                    String website = properties.path("heritage:website").asText(null);
                    String protectionTitle = properties.path("protection_title").asText(null);

                    JsonNode coordinates = geometry.path("coordinates");
                    if (coordinates.isArray() && coordinates.size() >= 2) {
                        double longitude = coordinates.get(0).asDouble();
                        double latitude = coordinates.get(1).asDouble();

                        // Create a transient monument object from JSON data.
                        Monument monumentFromJson = new Monument();
                        monumentFromJson.setName(name);
                        monumentFromJson.setDescription(description);
                        monumentFromJson.setLatitude(latitude);
                        monumentFromJson.setLongitude(longitude);
                        monumentFromJson.setWebsite(website);
                        monumentFromJson.setProtectionTitle(protectionTitle);
                        
                        // Place it in the map, keyed by name to handle duplicates.
                        monumentMap.put(name, monumentFromJson);
                    }
                }
            }
        }

        List<Monument> monumentsToSave = new ArrayList<>();
        for (Monument monumentFromJson : monumentMap.values()) {
            Optional<Monument> existingMonumentOpt = repository.findByName(monumentFromJson.getName());

            Monument monumentToSave = getMonumentToSave(monumentFromJson, existingMonumentOpt);
            monumentsToSave.add(monumentToSave);
        }

        return repository.saveAll(monumentsToSave);
    }

    private static Monument getMonumentToSave(Monument monumentFromJson, Optional<Monument> existingMonumentOpt) {
        Monument monumentToSave;
        if (existingMonumentOpt.isPresent()) {
            // If monument exists, update it.
            monumentToSave = existingMonumentOpt.get();
            monumentToSave.setDescription(monumentFromJson.getDescription());
            monumentToSave.setLatitude(monumentFromJson.getLatitude());
            monumentToSave.setLongitude(monumentFromJson.getLongitude());
            monumentToSave.setWebsite(monumentFromJson.getWebsite());
            monumentToSave.setProtectionTitle(monumentFromJson.getProtectionTitle());
        } else {
            // Otherwise, use the new one from the JSON.
            monumentToSave = monumentFromJson;
        }
        return monumentToSave;
    }
}
