package pt.estga.stonemark.services.proposal;

import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.proposals.MonumentData;

import java.util.Optional;

@Service
public class GpsExtractorService {

    public Optional<MonumentData> extractGpsData(MediaFile photo) {
        // This is a mock service. In a real implementation, this service would
        // extract GPS data from the photo's metadata.
        return Optional.empty();
    }
}
