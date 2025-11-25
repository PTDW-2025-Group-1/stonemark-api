package pt.estga.proposals.services;

import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.content.Mark;

import java.util.Optional;

@Service
public class MockMarkPatternSearchService {

    public Optional<Mark> findMatchingMark(MediaFile photo) {
        // This is a mock service. In a real implementation, this service would
        // use an image recognition service to find a matching mark pattern.
        return Optional.empty();
    }
}
