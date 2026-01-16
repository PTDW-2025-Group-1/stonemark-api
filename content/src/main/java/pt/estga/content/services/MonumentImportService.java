package pt.estga.content.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.administrative.entities.AdministrativeDivision;
import pt.estga.administrative.services.AdministrativeDivisionService;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MonumentRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonumentImportService {

    private final MonumentRepository repository;
    private final AdministrativeDivisionService administrativeDivisionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public int importFromGeoJson(InputStream inputStream) throws IOException {

        JsonNode root = objectMapper.readTree(inputStream);
        JsonNode features = root.path("features");

        Map<String, Monument> monumentMap = new LinkedHashMap<>();

        if (!features.isArray()) {
            return 0;
        }

        for (JsonNode feature : features) {

            JsonNode properties = feature.path("properties");
            if (!properties.isObject()) continue;

            String name = properties.path("name").asText(null);
            if (name == null || name.isBlank()) continue;

            JsonNode geometry = feature.path("geometry");
            if (!geometry.isObject()) continue;

            JsonNode coordinates = geometry.path("coordinates");
            if (!coordinates.isArray() || coordinates.size() != 2) continue;

            double lon = coordinates.get(0).asDouble(Double.NaN);
            double lat = coordinates.get(1).asDouble(Double.NaN);
            if (Double.isNaN(lat) || Double.isNaN(lon)) continue;

            Monument monument = new Monument();
            monument.setName(name);
            monument.setDescription(properties.path("description").asText(null));
            monument.setLatitude(lat);
            monument.setLongitude(lon);
            monument.setWebsite(properties.path("website").asText(null));
            monument.setProtectionTitle(properties.path("protection_title").asText(null));
            
            monument.setStreet(properties.path("addr:street").asText(null));
            monument.setHouseNumber(properties.path("addr:housenumber").asText(null));

            // Try to find parish by coordinates
            List<AdministrativeDivision> divisions = administrativeDivisionService.findByCoordinates(lat, lon);
            // Assuming adminLevel 8 is Parish
            Optional<AdministrativeDivision> parish = divisions.stream()
                    .filter(d -> d.getAdminLevel() == 8)
                    .findFirst();
            
            parish.ifPresent(monument::setParish);

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
        existing.setStreet(incoming.getStreet());
        existing.setHouseNumber(incoming.getHouseNumber());
        existing.setParish(incoming.getParish());
        return existing;
    }
}
