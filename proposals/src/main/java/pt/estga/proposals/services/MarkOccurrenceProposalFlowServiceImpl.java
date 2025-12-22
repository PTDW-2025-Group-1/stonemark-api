package pt.estga.proposals.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MarkService;
import pt.estga.content.services.MonumentService;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.detection.service.MarkSearchService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.entities.ProposedMark;
import pt.estga.proposals.entities.ProposedMonument;
import pt.estga.proposals.enums.SubmissionSource;
import pt.estga.proposals.repositories.ProposedMarkRepository;
import pt.estga.proposals.repositories.ProposedMonumentRepository;
import pt.estga.shared.enums.TargetType;
import pt.estga.shared.models.Location;
import pt.estga.user.services.UserService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarkOccurrenceProposalFlowServiceImpl implements MarkOccurrenceProposalFlowService {

    private final MarkOccurrenceProposalService proposalService;
    private final MediaService mediaService;
    private final MonumentService monumentService;
    private final MarkService markService;
    private final ProposedMarkRepository proposedMarkRepository;
    private final ProposedMonumentRepository proposedMonumentRepository;
    private final DetectionService detectionService;
    private final MarkSearchService markSearchService;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    private static final double COORDINATE_SEARCH_RANGE = 0.01;

    @Override
    @Transactional
    public MarkOccurrenceProposal initiate(Long userId, byte[] photoData, String filename, Double latitude, Double longitude) throws IOException {
        log.info("Initiating proposal for file: {}", filename);
        MediaFile mediaFile = mediaService.save(new ByteArrayInputStream(photoData), filename);
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setOriginalMediaFile(mediaFile);
        proposal.setLatitude(latitude);
        proposal.setLongitude(longitude);
        proposal.setSubmissionSource(SubmissionSource.TELEGRAM_BOT);
        if (userId != null) {
            userService.findById(userId).ifPresent(proposal::setCreatedBy);
        }
        return proposalService.create(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal analyzeMedia(Long proposalId) throws IOException {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        Resource photoResource = mediaService.loadFile(proposal.getOriginalMediaFile().getStoragePath());

        try (InputStream is = photoResource.getInputStream()) {
            byte[] photoData = is.readAllBytes();
            try (ByteArrayInputStream detectionInputStream = new ByteArrayInputStream(photoData)) {
                DetectionResult detectionResult = detectionService.detect(detectionInputStream, proposal.getOriginalMediaFile().getFileName());
                if (detectionResult != null && detectionResult.embedding() != null && !detectionResult.embedding().isEmpty()) {
                    List<Double> embeddedVector = detectionResult.embedding();
                    proposal.setEmbedding(embeddedVector);

                    List<String> suggestedMarkIds = markSearchService.searchMarks(embeddedVector);
                    if (suggestedMarkIds != null && !suggestedMarkIds.isEmpty()) {
                        try {
                            proposal.setSuggestedMarkIds(objectMapper.writeValueAsString(suggestedMarkIds));
                            log.info("Found {} suggested marks for proposal {}", suggestedMarkIds.size(), proposal.getId());
                        } catch (JsonProcessingException e) {
                            log.error("Error processing JSON for suggestedMarkIds for proposal {}: {}", proposal.getId(), e.getMessage());
                        }
                    } else {
                        log.info("No suggested marks found for proposal {}", proposal.getId());
                    }
                } else {
                    log.info("No embedding detected for proposal {}", proposal.getId());
                }
            }
        }

        handleGpsData(proposal, new Location(proposal.getLatitude(), proposal.getLongitude()));

        return proposalService.update(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal updatePhoto(Long proposalId, byte[] photoData, String filename) throws IOException {
        log.info("Updating photo for proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        MediaFile mediaFile = mediaService.save(new ByteArrayInputStream(photoData), filename);
        proposal.setOriginalMediaFile(mediaFile);
        return proposalService.update(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal selectMonument(Long proposalId, Long existingMonumentId) {
        log.info("User selected existing monument with ID: {} for proposal ID: {}", existingMonumentId, proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        clearMonumentSelections(proposal);

        monumentService.findById(existingMonumentId)
                .ifPresentOrElse(
                        proposal::setExistingMonument,
                        () -> {
                            log.error("Existing monument with ID {} not found for proposal ID {}", existingMonumentId, proposal.getId());
                            throw new RuntimeException("Selected monument not found.");
                        }
                );

        return proposalService.update(proposal);
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

        return proposalService.update(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal selectMark(Long proposalId, Long existingMarkId) {
        log.info("User selected existing mark with ID: {} for proposal ID: {}", existingMarkId, proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        clearMarkSelections(proposal);

        if (existingMarkId != null) {
            markService.findById(existingMarkId).ifPresentOrElse(
                    proposal::setExistingMark,
                    () -> {
                        log.error("Existing mark with ID {} not found for proposal ID {}", existingMarkId, proposal.getId());
                        throw new RuntimeException("Selected mark not found.");
                    }
            );
        }

        return proposalService.update(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal proposeMark(Long proposalId, String description) {
        log.info("User proposed a new mark for proposal ID: {}.", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        if (description == null || description.trim().isEmpty()) {
            clearMarkSelections(proposal);
            ProposedMark proposedMark = new ProposedMark();
            proposedMark.setDescription("");
            proposedMark.setMediaFile(proposal.getOriginalMediaFile());
            ProposedMark savedProposedMark = proposedMarkRepository.save(proposedMark);
            proposal.setProposedMark(savedProposedMark);
            return proposalService.update(proposal);
        }

        ProposedMark proposedMark = proposal.getProposedMark();
        if (proposedMark == null) {
            clearMarkSelections(proposal);
            proposedMark = new ProposedMark();
            proposal.setProposedMark(proposedMark);
        }

        proposedMark.setDescription(description);
        proposedMark.setMediaFile(proposal.getOriginalMediaFile());

        ProposedMark savedProposedMark = proposedMarkRepository.save(proposedMark);
        proposal.setProposedMark(savedProposedMark);

        return proposalService.update(proposal);
    }

    @Override
    public MarkOccurrenceProposal addNotesToProposal(Long proposalId, String notes) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setUserNotes(notes);
        return proposalService.update(proposal);
    }

    @Override
    public MarkOccurrenceProposal addLocationToProposal(Long proposalId, Double latitude, Double longitude) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setLatitude(latitude);
        proposal.setLongitude(longitude);
        return proposalService.update(proposal);
    }

    @Override
    public MarkOccurrenceProposal getProposal(Long proposalId) {
        return findProposalById(proposalId);
    }

    private MarkOccurrenceProposal findProposalById(Long proposalId) {
        return proposalService.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));
    }

    private void handleGpsData(MarkOccurrenceProposal proposal, Location gpsData) {
        log.info("GPS data found for proposal {}: Latitude={}, Longitude={}", proposal.getId(), gpsData.getLatitude(), gpsData.getLongitude());
        
        List<Monument> monuments = monumentService.findByCoordinatesInRange(
                gpsData.getLatitude(), gpsData.getLongitude(), COORDINATE_SEARCH_RANGE
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
