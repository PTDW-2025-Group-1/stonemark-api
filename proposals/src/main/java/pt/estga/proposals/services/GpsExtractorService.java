package pt.estga.proposals.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;
import pt.estga.shared.models.Location;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GpsExtractorService {

    private final MediaService mediaService;

    public Optional<Location> extractGpsData(MediaFile mediaFile) {
        if (mediaFile == null || mediaFile.getStoragePath() == null) {
            return Optional.empty();
        }

        try {
            Resource resource = mediaService.loadFile(mediaFile.getStoragePath());
            try (InputStream inputStream = resource.getInputStream()) {
                Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
                GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

                if (gpsDirectory != null) {
                    var geoLocation = gpsDirectory.getGeoLocation();
                    if (geoLocation != null) {
                        double latitude = geoLocation.getLatitude();
                        double longitude = geoLocation.getLongitude();
                        log.info("Extracted GPS data: Latitude={}, Longitude={}", latitude, longitude);
                        return Optional.of(new Location(latitude, longitude));
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            log.warn("Could not extract GPS data from media file {}: {}", mediaFile.getId(), e.getMessage());
        }
        return Optional.empty();
    }
}
