package pt.estga.content.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MonumentRepository;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.entities.LogicalLevel;
import pt.estga.territory.services.AdministrativeDivisionService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonumentImportService {

    private final MonumentRepository repository;
    private final AdministrativeDivisionService administrativeDivisionService;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public int importFromGeoJson(InputStream inputStream) throws IOException {

        JsonNode root = objectMapper.readTree(inputStream);
        JsonNode features = root.path("features");

        if (!features.isArray()) {
            return 0;
        }

        // Phase 1: Build an in-memory spatial index of all parishes.
        log.info("Building parish spatial index...");
        STRtree parishIndex = new STRtree();
        List<AdministrativeDivision> parishes = administrativeDivisionService.findByOsmAdminLevel(8);
        for (AdministrativeDivision parish : parishes) {
            if (parish.getGeometry() != null) {
                parishIndex.insert(parish.getGeometry().getEnvelopeInternal(), parish);
            }
        }
        parishIndex.build();
        log.info("Parish index built with {} parishes.", parishes.size());

        // Phase 2: Process monuments from GeoJSON
        Map<String, Monument> monumentMap = new LinkedHashMap<>();

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

            // Find parish using the in-memory spatial index (no DB call here)
            findParish(parishIndex, lon, lat).ifPresent(monument::setParish);

            if (monumentMap.containsKey(name)) {
                log.warn("Duplicate monument name found: '{}'. The last entry will be used. Consider a more robust unique identifier.", name);
            }
            monumentMap.put(name, monument);
        }

        // Phase 3: Persist monuments
        List<Monument> toSave = new ArrayList<>();
        for (Monument incoming : monumentMap.values()) {
            repository.findByName(incoming.getName())
                    .ifPresentOrElse(
                            existing -> {
                                incoming.setId(existing.getId()); // Set ID to trigger an update
                                toSave.add(incoming);
                            },
                            () -> toSave.add(incoming)
                    );
        }

        repository.saveAll(toSave);
        return toSave.size();
    }

    private Optional<AdministrativeDivision> findParish(STRtree parishIndex, double lon, double lat) {
        Point monumentLocation = geometryFactory.createPoint(new Coordinate(lon, lat));
        
        @SuppressWarnings("unchecked")
        List<AdministrativeDivision> potentialParishes = parishIndex.query(monumentLocation.getEnvelopeInternal());

        return potentialParishes.stream()
                .filter(parish -> parish.getGeometry().contains(monumentLocation))
                .findFirst();
    }
}
