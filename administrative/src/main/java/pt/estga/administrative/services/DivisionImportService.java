package pt.estga.administrative.services;

import com.fasterxml.jackson.databind.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Service;
import pt.estga.administrative.entities.AdministrativeDivision;
import pt.estga.administrative.repositories.AdministrativeDivisionRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for importing administrative divisions from OSM PBF files.
 *
 * <p>This service uses the `osmium` command-line tool to extract administrative boundaries
 * from a PBF file and convert them to GeoJSON sequences. The GeoJSON features are then
 * parsed and saved to the database.</p>
 *
 * <p><strong>AI Usage Disclosure:</strong> This class was refactored with the assistance of AI
 * to improve the filtering logic and remove unnecessary fields. The filtering logic now strictly
 * adheres to specific administrative levels (6, 7, 8) and ensures only valid administrative
 * boundaries are imported.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DivisionImportService {

    private final AdministrativeDivisionRepository repository;
    private final ObjectMapper objectMapper;
    private final DivisionParentMappingWorker parentMappingWorker;

    public int importFromPbf(InputStream pbfStream) throws Exception {

        Path pbfFile = Files.createTempFile("portugal-admin-", ".osm.pbf");
        Files.copy(pbfStream, pbfFile, StandardCopyOption.REPLACE_EXISTING);

        ProcessBuilder pb = new ProcessBuilder(
                "osmium", "export",
                pbfFile.toAbsolutePath().toString(),
                "--geometry-types=polygon",
                "--attributes=type,id,version,timestamp,changeset,uid,user",
                "--output-format=geojsonseq"
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        String line;
        List<AdministrativeDivision> batch = new ArrayList<>(1000);
        int count = 0;
        GeoJsonReader geoJsonReader = new GeoJsonReader();

        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty() && line.charAt(0) == 0x1E) {
                line = line.substring(1);
            }

            JsonNode feature = objectMapper.readTree(line);
            JsonNode props = feature.get("properties");
            JsonNode geomNode = feature.get("geometry");

            if (props == null || geomNode == null) continue;

            if (!"administrative".equals(props.path("boundary").asText())) {
                continue;
            }

            int adminLevel = props.path("admin_level").asInt(-1);
            // Allow level 4 (Autonomous Regions), 6 (Districts), 7 (Municipalities), 8 (Parishes)
            if (adminLevel != 4 && adminLevel != 6 && adminLevel != 7 && adminLevel != 8) {
                continue;
            }

            String name = resolveName(props);
            if (name == null || name.isBlank()) {
                continue;
            }

            // Extract OSM ID
            long osmId = 0;
            if (feature.hasNonNull("id")) {
                osmId = feature.get("id").asLong();
            } else if (props.hasNonNull("id")) {
                osmId = props.get("id").asLong();
            } else if (props.hasNonNull("@id")) {
                osmId = props.get("@id").asLong();
            }

            if (osmId == 0) {
                log.warn("Skipping division '{}' because no valid OSM ID was found.", name);
                continue;
            }

            // Filter out level 4 that are not Azores or Madeira by ID
            // Azores: 1629146, Madeira: 1629145
            if (adminLevel == 4) {
                if (osmId == 1629146 || osmId == 1629145) {
                    // Treat them as level 6 (Districts) to simplify hierarchy
                    adminLevel = 6;
                } else {
                    log.info("Skipping level 4 division '{}' (ID: {}) as it is not Azores or Madeira.", name, osmId);
                    continue;
                }
            }

            Geometry geometry = geoJsonReader.read(objectMapper.writeValueAsString(geomNode));
            geometry.setSRID(4326);
            
            // Fix invalid geometries
            if (!geometry.isValid()) {
                geometry = geometry.buffer(0);
            }

            // Check if exists
            Optional<AdministrativeDivision> existingOpt = repository.findById(osmId);
            AdministrativeDivision div;

            if (existingOpt.isPresent()) {
                div = existingOpt.get();
                div.setName(name);
                div.setAdminLevel(adminLevel);
                div.setGeometry(geometry);
                // Parent will be recalculated later
            } else {
                div = AdministrativeDivision.builder()
                        .id(osmId)
                        .name(name)
                        .adminLevel(adminLevel)
                        .geometry(geometry)
                        .build();
            }

            batch.add(div);
            count++;

            if (batch.size() == 1000) {
                repository.saveAll(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            repository.saveAll(batch);
        }

        int exit = process.waitFor();
        if (exit != 0) {
            throw new IllegalStateException("osmium failed with exit code " + exit);
        }

        Files.deleteIfExists(pbfFile);

        // Trigger parent mapping asynchronously
        parentMappingWorker.calculateParents();

        return count;
    }

    private String resolveName(JsonNode props) {
        if (props.hasNonNull("name")) {
            return props.get("name").asText();
        }
        if (props.hasNonNull("name:pt")) {
            return props.get("name:pt").asText();
        }
        return null;
    }
}
