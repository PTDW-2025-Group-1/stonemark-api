package pt.estga.territory.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Service;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.entities.OsmType;
import pt.estga.territory.repositories.AdministrativeDivisionRepository;

import java.io.BufferedReader;
import java.io.IOException;
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
        try {
            Files.copy(pbfStream, pbfFile, StandardCopyOption.REPLACE_EXISTING);

            // Osmium command to export administrative boundaries as a GeoJSON sequence.
            // The --attributes flag includes OSM metadata in the properties of the GeoJSON features.
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
                if (line.isBlank()) {
                    continue;
                }
                // The GeoJSON Text Sequence format uses a record separator (0x1E) before each feature.
                if (line.charAt(0) == 0x1E) {
                    line = line.substring(1);
                }

                JsonNode feature;
                try {
                    feature = objectMapper.readTree(line);
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse GeoJSON line from osmium: {}", line);
                    throw new IOException("Failed to parse GeoJSON output from osmium. The line was: '" + line + "'. Make sure 'osmium-tool' is installed and in the system's PATH.", e);
                }

                JsonNode props = feature.get("properties");
                JsonNode geomNode = feature.get("geometry");

                if (props == null || geomNode == null) continue;

                if (!"administrative".equals(props.path("boundary").asText())) {
                    continue;
                }

                if (!props.hasNonNull("admin_level")) {
                    continue;
                }
                int adminLevel = props.path("admin_level").asInt(-1);

                String name = resolveName(props);
                String namePt = resolveNamePt(props);
                
                if ((name == null || name.isBlank()) && (namePt == null || namePt.isBlank())) {
                    continue;
                }

                // --- Start of OSM ID and Type parsing ---
                // The OSM ID and type are included in the feature's properties because of the --attributes flag.
                // They are prefixed with '@' to avoid conflicts with OSM tags.
                long osmId = props.path("@id").asLong(0);
                String typeStr = props.path("@type").asText();
                OsmType osmType;

                if (osmId == 0 || typeStr.isBlank()) {
                    log.warn("Skipping division '{}' because of missing OSM ID or type from properties. ID: {}, Type: {}", name, osmId, typeStr);
                    continue;
                }

                try {
                    osmType = OsmType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping division '{}' due to invalid OSM type: {}", name, typeStr);
                    continue;
                }
                // --- End of OSM ID and Type parsing ---
                
                // Construct the composite ID used by the application
                String compositeId = osmType + ":" + osmId;

                Geometry geometry = geoJsonReader.read(objectMapper.writeValueAsString(geomNode));
                geometry.setSRID(4326);
                
                if (!geometry.isValid()) {
                    log.warn("Fixing invalid geometry for OSM ID: {}", compositeId);
                    geometry = geometry.buffer(0);
                    if (!geometry.isValid()) {
                        log.error("Geometry still invalid after buffer(0) for OSM ID: {}", compositeId);
                    }
                }

                double centroidLat = geometry.getCentroid().getY();
                double areaDegrees = geometry.getArea();
                // Area calculation is approximate and heuristic-only.
                // TODO: Replace with proper reprojection to EPSG:3035 (ETRS89-LAEA) or similar equal-area projection for accurate results.
                double areaKm2 = areaDegrees * (111.32 * 111.32 * Math.cos(Math.toRadians(centroidLat)));


                Optional<AdministrativeDivision> existingOpt = repository.findById(compositeId);
                AdministrativeDivision div;

                if (existingOpt.isPresent()) {
                    div = existingOpt.get();
                    div.setName(name);
                    div.setNamePt(namePt);
                    div.setOsmAdminLevel(adminLevel);
                    div.setGeometry(geometry);
                    div.setAreaKm2(areaKm2);
                    div.setOsmType(osmType);
                    div.setOsmId(osmId);
                    div.setBoundary("administrative");
                    div.setCountryCode("PT");
                } else {
                    div = AdministrativeDivision.builder()
                            .id(compositeId)
                            .osmId(osmId)
                            .osmType(osmType)
                            .osmAdminLevel(adminLevel)
                            .boundary("administrative")
                            .name(name)
                            .namePt(namePt)
                            .geometry(geometry)
                            .areaKm2(areaKm2)
                            .countryCode("PT")
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
            
            parentMappingWorker.calculateParents(true, true);

            return count;
        } finally {
            Files.deleteIfExists(pbfFile);
        }
    }

    private String resolveName(JsonNode props) {
        if (props.hasNonNull("name")) {
            return props.get("name").asText();
        }
        return null;
    }

    private String resolveNamePt(JsonNode props) {
        if (props.hasNonNull("name:pt")) {
            return props.get("name:pt").asText();
        }
        return null;
    }
}
