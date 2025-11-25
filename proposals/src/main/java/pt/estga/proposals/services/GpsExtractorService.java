package pt.estga.proposals.services;

import org.springframework.stereotype.Service;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.models.Location;

import java.util.Optional;

@Service
public class GpsExtractorService {

    public Optional<Location> extractGpsData(MediaFile photo) {
        // This is a mock service. In a real implementation, this service would
        // extract GPS data from the photo's metadata.
        return Optional.empty();
    }
}
