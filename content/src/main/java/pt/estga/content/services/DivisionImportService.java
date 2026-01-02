package pt.estga.content.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.AdministrativeDivision;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DivisionImportService {

    private final ObjectMapper objectMapper;
    private final AdministrativeDivisionService administrativeDivisionService;

    @Transactional
    public int overpass(String geoJson) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(geoJson);
        JsonNode features = root.path("features");

        // Fallback: if root is an array, assume it's the list of features
        if (features.isMissingNode() && root.isArray()) {
            features = root;
        }

        if (!features.isArray()) {
            log.warn("Import failed: 'features' is not an array or missing. Root type: {}", root.getNodeType());
            return 0;
        }

        log.info("Processing {} features from GeoJSON...", features.size());

        List<JsonNode> validFeatures = new ArrayList<>();
        Set<String> namesToFetch = new HashSet<>();

        // 1. Collect names and valid features
        for (JsonNode feature : features) {
            JsonNode properties = feature.path("properties");
            
            if (properties.isMissingNode()) {
                log.debug("Skipping feature: missing 'properties'.");
                continue;
            }

            if (properties.has("name") && !properties.get("name").isNull()) {
                String name = properties.get("name").asText();
                if (name != null && !name.isBlank()) {
                    namesToFetch.add(name);
                    validFeatures.add(feature);
                } else {
                    log.debug("Skipping feature: 'name' is blank.");
                }
            } else {
                log.debug("Skipping feature: missing 'name' property.");
            }
        }

        log.info("Found {} valid features to import.", validFeatures.size());

        if (validFeatures.isEmpty()) {
            return 0;
        }

        // 2. Bulk fetch existing divisions
        List<AdministrativeDivision> existingDivisions = administrativeDivisionService.findAllByNameIn(namesToFetch);
        
        // Map by Key: Name::AdminLevel
        Map<String, AdministrativeDivision> divisionMap = existingDivisions.stream()
                .collect(Collectors.toMap(
                        this::generateKey,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<AdministrativeDivision> divisionsToSave = new ArrayList<>();

        // 3. Process features
        for (JsonNode feature : validFeatures) {
            JsonNode properties = feature.path("properties");
            String name = properties.path("name").asText();
            String adminLevel = properties.path("admin_level").asText(null);
            String borderType = properties.path("border_type").asText(null);
            JsonNode geometry = feature.path("geometry");

            // Build or Update Entity
            String key = generateKey(name, adminLevel);
            AdministrativeDivision division = divisionMap.get(key);

            if (division == null) {
                division = new AdministrativeDivision();
                division.setName(name);
                // Update map to handle duplicates within the same import
                divisionMap.put(key, division);
            }

            division.setAdminLevel(adminLevel);
            division.setBorderType(borderType);
            if (geometry != null) {
                division.setGeometryJson(geometry.toString());
            }
            divisionsToSave.add(division);
        }

        // 4. Save all changes
        // Deduplicate by reference in case the same entity was added multiple times
        List<AdministrativeDivision> uniqueDivisions = divisionsToSave.stream()
                .distinct()
                .toList();

        administrativeDivisionService.createOrUpdateAll(uniqueDivisions);
        
        log.info("Successfully saved {} divisions.", uniqueDivisions.size());

        return validFeatures.size();
    }

    private String generateKey(AdministrativeDivision div) {
        return generateKey(div.getName(), div.getAdminLevel());
    }

    private String generateKey(String name, String adminLevel) {
        return name + "::" + (adminLevel != null ? adminLevel : "NULL");
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }
}
