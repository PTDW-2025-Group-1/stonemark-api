package pt.estga.content.services;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.AdministrativeDivision;
import pt.estga.content.repositories.AdministrativeDivisionRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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

    @Transactional
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

        repository.deleteAllInBatch();

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
            if (adminLevel != 6 && adminLevel != 7 && adminLevel != 8) {
                continue;
            }

            String name = resolveName(props);
            if (name == null || name.isBlank()) {
                continue;
            }

            Geometry geometry = geoJsonReader.read(objectMapper.writeValueAsString(geomNode));
            geometry.setSRID(4326);

            AdministrativeDivision div = AdministrativeDivision.builder()
                    .name(name)
                    .adminLevel(adminLevel)
                    .geometry(geometry)
                    .build();

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
