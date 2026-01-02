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

@Service
@RequiredArgsConstructor
public class MonumentImportService {

    private final MonumentRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public int overpass(String monumentJson) throws JsonProcessingException {

        JsonNode root = objectMapper.readTree(monumentJson);
        JsonNode elements = root.path("elements");

        Map<String, Monument> monumentMap = new LinkedHashMap<>();

        if (!elements.isArray()) {
            return 0;
        }

        for (JsonNode element : elements) {

            JsonNode tags = element.path("tags");
            if (!tags.isObject()) continue;

            String name = tags.path("name").asText(null);
            if (name == null || name.isBlank()) continue;

            double lat = element.path("lat").asDouble(Double.NaN);
            double lon = element.path("lon").asDouble(Double.NaN);
            if (Double.isNaN(lat) || Double.isNaN(lon)) continue;

            Monument monument = new Monument();
            monument.setName(name);
            monument.setDescription(tags.path("description").asText(null));
            monument.setLatitude(lat);
            monument.setLongitude(lon);
            monument.setWebsite(tags.path("website").asText(null));
            monument.setProtectionTitle(tags.path("protection_title").asText(null));

            // Note: Administrative division is no longer set here as per request to separate concerns
            // It might be set later or inferred from coordinates if needed, 
            // but the request specifically asked to "refactor monument import service to treat only monuments"

            monumentMap.put(name, monument);
        }

        List<Monument> toSave = new ArrayList<>();
        for (Monument m : monumentMap.values()) {
            repository.findByName(m.getName())
                    .ifPresentOrElse(
                            existing -> toSave.add(update(existing, m)),
                            () -> toSave.add(m)
                    );
        }

        repository.saveAll(toSave);
        return toSave.size();
    }

    private Monument update(Monument existing, Monument incoming) {
        existing.setDescription(incoming.getDescription());
        existing.setLatitude(incoming.getLatitude());
        existing.setLongitude(incoming.getLongitude());
        existing.setWebsite(incoming.getWebsite());
        existing.setProtectionTitle(incoming.getProtectionTitle());
        // We do not update district/municipality/parish here anymore as they are not passed in
        return existing;
    }
}
