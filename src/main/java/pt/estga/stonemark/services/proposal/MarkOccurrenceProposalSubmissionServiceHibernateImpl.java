package pt.estga.stonemark.services.proposal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.content.Mark;
import pt.estga.stonemark.entities.content.Monument;
import pt.estga.stonemark.entities.proposals.MarkOccurrenceProposal;
import pt.estga.stonemark.entities.proposals.MonumentData;
import pt.estga.stonemark.repositories.MediaRepository;
import pt.estga.stonemark.repositories.proposals.MarkOccurrenceProposalRepository;
import pt.estga.stonemark.services.content.MonumentService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceProposalSubmissionServiceHibernateImpl implements MarkOccurrenceProposalSubmissionService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final MediaRepository mediaRepository;
    private final GpsExtractorService gpsExtractorService;
    private final MonumentService monumentService;
    private final MockMarkPatternSearchService markPatternSearchService;
    private static final double COORDINATE_SEARCH_RANGE = 0.01; // Approx 1.11km

    @Override
    public MarkOccurrenceProposal submitProposal(MediaFile photo, String monumentName, double latitude, double longitude) {
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setOriginalMediaFile(photo);

        Optional<MonumentData> gpsData = gpsExtractorService.extractGpsData(photo);

        Optional<Monument> foundMonument = Optional.empty();

        if (gpsData.isPresent()) {
            List<Monument> monuments = monumentService.findByCoordinatesInRange(gpsData.get().getLatitude(), gpsData.get().getLongitude(), COORDINATE_SEARCH_RANGE);
            if (!monuments.isEmpty()) {
                foundMonument = Optional.of(monuments.getFirst());
            }
        } else if (monumentName != null && !monumentName.isBlank()) {
            List<Monument> monuments = monumentService.findByNameContaining(monumentName);
            if (!monuments.isEmpty()) {
                foundMonument = Optional.of(monuments.getFirst());
            }
        } else if (latitude != 0 && longitude != 0) {
            List<Monument> monuments = monumentService.findByCoordinatesInRange(latitude, longitude, COORDINATE_SEARCH_RANGE);
            if (!monuments.isEmpty()) {
                foundMonument = Optional.of(monuments.getFirst());
            }
        }

        if (foundMonument.isPresent()) {
            proposal.setExistingMonument(foundMonument.get());
        } else {
            proposal.setProposedMonumentData(MonumentData.builder()
                    .name(monumentName)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build());
        }

        Optional<Mark> foundMark = markPatternSearchService.findMatchingMark(photo);
        foundMark.ifPresent(proposal::setExistingMark);

        MarkOccurrenceProposal savedProposal = proposalRepository.save(proposal);

        photo.setTargetId(savedProposal.getId());
        mediaRepository.save(photo);

        return savedProposal;
    }
}
