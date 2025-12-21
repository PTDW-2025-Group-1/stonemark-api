package pt.estga.proposals.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.content.repositories.MonumentRepository;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.detection.service.MarkSearchService;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.enums.TargetType;
import pt.estga.file.services.MediaService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.entities.ProposedMark;
import pt.estga.proposals.entities.ProposedMonument;
import pt.estga.proposals.enums.SubmissionSource;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposals.repositories.ProposedMarkRepository;
import pt.estga.proposals.repositories.ProposedMonumentRepository;
import pt.estga.shared.models.Location;
import pt.estga.user.repositories.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarkOccurrenceProposalFlowServiceHibernateImpl implements MarkOccurrenceProposalFlowService {

    // Todo: refactor to use services instead of repositories
    private final MarkOccurrenceProposalRepository proposalRepository;
    private final MediaService mediaService;
    private final MonumentRepository monumentRepository;
    private final MarkRepository markRepository;
    private final ProposedMarkRepository proposedMarkRepository;
    private final ProposedMonumentRepository proposedMonumentRepository;
    private final DetectionService detectionService;
    private final MarkSearchService markSearchService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    private static final double COORDINATE_SEARCH_RANGE = 0.01;

    @Override
    @Transactional
    public MarkOccurrenceProposal initiate(Long userId, byte[] photoData, String filename) throws IOException {
        log.info("Initiating proposal for file: {}", filename);
        MediaFile mediaFile = mediaService.save(photoData, filename, TargetType.PROPOSAL);
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setOriginalMediaFile(mediaFile);
        proposal.setSubmissionSource(SubmissionSource.TELEGRAM_BOT);
        if (userId != null) {
            userRepository.findById(userId).ifPresent(proposal::setCreatedBy);
        }
        MarkOccurrenceProposal savedProposal = proposalRepository.save(proposal);
        log.debug("Proposal initiated with ID: {}", savedProposal.getId());

        return detectAndSearch(photoData, filename, mediaFile, proposal, savedProposal);
    }

    @NonNull
    private MarkOccurrenceProposal detectAndSearch(byte[] photoData, String filename, MediaFile mediaFile, MarkOccurrenceProposal proposal, MarkOccurrenceProposal savedProposal) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(photoData)) {
            DetectionResult detectionResult = detectionService.detect(is, filename);
            if (detectionResult != null && detectionResult.embedding() != null && !detectionResult.embedding().isEmpty()) {
                List<Double> embeddedVector = detectionResult.embedding();
                proposal.setEmbedding(embeddedVector);

                // suggestedMarkIds still uses ObjectMapper to store as String
                List<String> suggestedMarkIds = markSearchService.searchMarks(embeddedVector);
                if (suggestedMarkIds != null && !suggestedMarkIds.isEmpty()) {
                    try {
                        proposal.setSuggestedMarkIds(objectMapper.writeValueAsString(suggestedMarkIds));
                        log.info("Found {} suggested marks for proposal {}", suggestedMarkIds.size(), savedProposal.getId());
                    } catch (JsonProcessingException e) {
                        log.error("Error processing JSON for suggestedMarkIds for proposal {}: {}", savedProposal.getId(), e.getMessage());
                    }
                } else {
                    log.info("No suggested marks found for proposal {}", savedProposal.getId());
                }
            } else {
                log.info("No embedding detected for proposal {}", savedProposal.getId());
            }
        }

        return proposalRepository.save(savedProposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal updatePhoto(Long proposalId, byte[] photoData, String filename) throws IOException {
        log.info("Updating photo for proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setLatitude(null);
        proposal.setLongitude(null);
        MediaFile mediaFile = mediaService.save(photoData, filename, TargetType.PROPOSAL);
        proposal.setOriginalMediaFile(mediaFile);

        // Perform detection and search
        return detectAndSearch(photoData, filename, mediaFile, proposal, proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal selectMonument(Long proposalId, Long existingMonumentId) {
        log.info("User selected existing monument with ID: {} for proposal ID: {}", existingMonumentId, proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        clearMonumentSelections(proposal);

        monumentRepository.findById(existingMonumentId)
                .ifPresentOrElse(
                        proposal::setExistingMonument,
                        () -> {
                            log.error("Existing monument with ID {} not found for proposal ID {}", existingMonumentId, proposal.getId());
                            throw new RuntimeException("Selected monument not found.");
                        }
                );

        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal proposeMonument(Long proposalId, String name, Double latitude, Double longitude) {
        log.info("User proposed a new monument for proposal ID: {}. Name: {}, Latitude: {}, Longitude: {}", proposalId, name, latitude, longitude);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        clearMonumentSelections(proposal);

        ProposedMonument proposedMonument = ProposedMonument.builder()
                .name(name)
                .latitude(proposal.getLatitude())
                .longitude(proposal.getLongitude())
                .build();

        ProposedMonument savedProposedMonument = proposedMonumentRepository.save(proposedMonument);
        proposal.setProposedMonument(savedProposedMonument);

        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal selectMark(Long proposalId, Long existingMarkId) {
        log.info("User selected existing mark with ID: {} for proposal ID: {}", existingMarkId, proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        clearMarkSelections(proposal);

        if (existingMarkId != null) {
            markRepository.findById(existingMarkId).ifPresentOrElse(
                    proposal::setExistingMark,
                    () -> {
                        log.error("Existing mark with ID {} not found for proposal ID {}", existingMarkId, proposal.getId());
                        throw new RuntimeException("Selected mark not found.");
                    }
            );
        }

        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal proposeMark(Long proposalId, String description) {
        log.info("User proposed a new mark for proposal ID: {}.", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        ProposedMark proposedMark = proposal.getProposedMark();
        if (proposedMark == null) {
            clearMarkSelections(proposal);
            proposedMark = new ProposedMark();
            proposal.setProposedMark(proposedMark);
        }

        proposedMark.setDescription(Optional.ofNullable(description).orElse(""));
        proposedMark.setMediaFile(proposal.getOriginalMediaFile());

        ProposedMark savedProposedMark = proposedMarkRepository.save(proposedMark);
        proposal.setProposedMark(savedProposedMark);

        return proposalRepository.save(proposal);
    }

    @Override
    public MarkOccurrenceProposal requestNewMark(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        return proposalRepository.save(proposal);
    }

    @Override
    public MarkOccurrenceProposal requestNewMonument(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        return proposalRepository.save(proposal);
    }

    @Override
    public MarkOccurrenceProposal confirmMonumentLocation(Long proposalId, boolean confirmed) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        if (confirmed) {
            Location locationToUse = null;

            // Prioritize user-provided monument location if available
            if (proposal.getProposedMonument() != null &&
                proposal.getProposedMonument().getLatitude() != null &&
                proposal.getProposedMonument().getLongitude() != null) {
                log.info("Using user-provided proposed monument location for proposal {}", proposal.getId());
                locationToUse = new Location(proposal.getProposedMonument().getLatitude(), proposal.getProposedMonument().getLongitude());
            } else if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
                // Fallback to cached GPS data from the photo
                log.info("Using cached GPS data from photo for proposal {}", proposal.getId());
                locationToUse = new Location(proposal.getLatitude(), proposal.getLongitude());
            } else {
                log.warn("No cached GPS data or proposed monument location found for proposal {}.", proposal.getId());
            }

            if (locationToUse != null) {
                handleGpsData(proposal, locationToUse);
            }
        }
        return proposalRepository.save(proposal);
    }

    @Override
    public MarkOccurrenceProposal addNotesToProposal(Long proposalId, String notes) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setUserNotes(notes);
        return proposalRepository.save(proposal);
    }

    @Override
    public MarkOccurrenceProposal addLocationToProposal(Long proposalId, Double latitude, Double longitude) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setLatitude(latitude);
        proposal.setLongitude(longitude);
        handleGpsData(proposal, new Location(latitude, longitude));
        return proposalRepository.save(proposal);
    }

    private MarkOccurrenceProposal findProposalById(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));
    }

    private void handleGpsData(MarkOccurrenceProposal proposal, Location gpsData) {
        log.info("GPS data found for proposal {}: Latitude={}, Longitude={}", proposal.getId(), gpsData.getLatitude(), gpsData.getLongitude());
        
        double minLat = gpsData.getLatitude() - COORDINATE_SEARCH_RANGE;
        double maxLat = gpsData.getLatitude() + COORDINATE_SEARCH_RANGE;
        double minLon = gpsData.getLongitude() - COORDINATE_SEARCH_RANGE;
        double maxLon = gpsData.getLongitude() + COORDINATE_SEARCH_RANGE;
        
        log.info("Searching for monuments between lat [{}, {}] and lon [{}, {}]", minLat, maxLat, minLon, maxLon);
        
        List<Monument> monuments = monumentRepository.findByLatitudeBetweenAndLongitudeBetween(
                minLat, maxLat, minLon, maxLon
        );

        if (!monuments.isEmpty()) {
            log.info("Found {} existing monuments for proposal {}", monuments.size(), proposal.getId());
            try {
                List<String> monumentIds = monuments.stream()
                                                    .map(m -> m.getId().toString())
                                                    .toList();
                proposal.setSuggestedMonumentIds(objectMapper.writeValueAsString(monumentIds));
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON for suggestedMonumentIds for proposal {}: {}", proposal.getId(), e.getMessage());
            }
        } else {
            log.info("No existing monument found near GPS coordinates for proposal {}", proposal.getId());
        }
    }

    private void clearMonumentSelections(MarkOccurrenceProposal proposal) {
        proposal.setExistingMonument(null);
        if (proposal.getProposedMonument() != null) {
            proposedMonumentRepository.delete(proposal.getProposedMonument());
            proposal.setProposedMonument(null);
        }
    }

    private void clearMarkSelections(MarkOccurrenceProposal proposal) {
        proposal.setExistingMark(null);
        if (proposal.getProposedMark() != null) {
            proposedMarkRepository.delete(proposal.getProposedMark());
            proposal.setProposedMark(null);
        }
    }
}
