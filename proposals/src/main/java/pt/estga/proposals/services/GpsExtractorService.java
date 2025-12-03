package pt.estga.proposals.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.FileStorageService;
import pt.estga.shared.models.Location;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class GpsExtractorService {

    private final FileStorageService fileStorageService;

    public GpsExtractorService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public Optional<Location> extractGpsData(MediaFile photo) {
        try {
            Resource resource = fileStorageService.loadFile(photo.getStoragePath());
            try (InputStream inputStream = resource.getInputStream()) {
                Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
                GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

                if (gpsDirectory != null) {
                    GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                    if (geoLocation != null) {
                        return Optional.of(new Location(geoLocation.getLatitude(), geoLocation.getLongitude()));
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            // Log the exception (optional)
        }
        return Optional.empty();
    }
}
