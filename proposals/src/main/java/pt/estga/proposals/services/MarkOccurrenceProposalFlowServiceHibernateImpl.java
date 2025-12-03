package pt.estga.proposals.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.content.repositories.MonumentRepository;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.detection.service.MarkSearchService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.enums.TargetType;
import pt.estga.file.services.MediaService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.entities.ProposedMark;
import pt.estga.proposals.entities.ProposedMonument;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposals.repositories.ProposedMarkRepository;
import pt.estga.proposals.repositories.ProposedMonumentRepository;
import pt.estga.shared.models.Location;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarkOccurrenceProposalFlowServiceHibernateImpl implements MarkOccurrenceProposalFlowService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final MediaService mediaService;
    private final GpsExtractorService gpsExtractorService;
    private final MonumentRepository monumentRepository;
    private final MarkRepository markRepository;
    private final ProposedMarkRepository proposedMarkRepository;
    private final ProposedMonumentRepository proposedMonumentRepository;
    private final DetectionService detectionService;
    private final MarkSearchService markSearchService;
    private final ObjectMapper objectMapper;

    private static final double COORDINATE_SEARCH_RANGE = 0.01;

    @Override
    @Transactional
    public MarkOccurrenceProposal initiate(byte[] photoData, String filename) throws IOException {
        log.info("Initiating proposal for file: {}", filename);
        MediaFile mediaFile = mediaService.save(photoData, filename, TargetType.PROPOSAL);
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setOriginalMediaFile(mediaFile);
        proposal.setStatus(ProposalStatus.IN_PROGRESS);
        MarkOccurrenceProposal savedProposal = proposalRepository.save(proposal);
        log.debug("Proposal initiated with ID: {}", savedProposal.getId());

        // Perform detection and search
        try (ByteArrayInputStream is = new ByteArrayInputStream(photoData)) {
            DetectionResult detectionResult = detectionService.detect(is, filename);
            if (detectionResult != null && detectionResult.embedding() != null && !detectionResult.embedding().isEmpty()) {
                List<Double> embeddedVector = detectionResult.embedding();
                proposal.setEmbedding(embeddedVector); // Corrected: Pass List<Double> directly

                // suggestedMarkIds still uses ObjectMapper to store as String
                List<String> suggestedMarkIds = markSearchService.searchMarks(embeddedVector);
                if (suggestedMarkIds != null && !suggestedMarkIds.isEmpty()) {
                    proposal.setSuggestedMarkIds(objectMapper.writeValueAsString(suggestedMarkIds));
                    log.info("Found {} suggested marks for proposal {}", suggestedMarkIds.size(), savedProposal.getId());
                } else {
                    log.info("No suggested marks found for proposal {}", savedProposal.getId());
                }
            } else {
                log.info("No embedding detected for proposal {}", savedProposal.getId());
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON for suggestedMarkIds for proposal {}: {}", savedProposal.getId(), e.getMessage());
        }


        Optional<Location> gpsData = gpsExtractorService.extractGpsData(mediaFile);
        if (gpsData.isPresent()) {
            handleGpsData(savedProposal, gpsData.get());
        } else {
            log.info("No GPS data found for proposal {}", savedProposal.getId());
            savedProposal.setStatus(ProposalStatus.AWAITING_MONUMENT_INFO);
        }

        // Determine the next status based on search results
        if (proposal.getSuggestedMarkIds() != null && !proposal.getSuggestedMarkIds().isEmpty()) {
            proposal.setStatus(ProposalStatus.AWAITING_MARK_SELECTION);
        } else if (proposal.getExistingMonument() != null) {
            proposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
        } else {
            proposal.setStatus(ProposalStatus.AWAITING_MONUMENT_INFO);
        }

        return proposalRepository.save(savedProposal);
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

        proposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
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
                .latitude(latitude)
                .longitude(longitude)
                .build();

        ProposedMonument savedProposedMonument = proposedMonumentRepository.save(proposedMonument);
        proposal.setProposedMonument(savedProposedMonument);

        proposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal selectMark(Long proposalId, Long existingMarkId) {
        log.info("User selected existing mark with ID: {} for proposal ID: {}", existingMarkId, proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        clearMarkSelections(proposal);

        markRepository.findById(existingMarkId).ifPresentOrElse(
                proposal::setExistingMark,
                () -> {
                    log.error("Existing mark with ID {} not found for proposal ID {}", existingMarkId, proposal.getId());
                    throw new RuntimeException("Selected mark not found.");
                }
        );

        proposal.setStatus(ProposalStatus.READY_TO_SUBMIT);
        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal proposeMark(Long proposalId, String title, String description) {
        log.info("User proposed a new mark for proposal ID: {}. Name: {}", proposalId, title);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        clearMarkSelections(proposal);

        ProposedMark proposedMark = new ProposedMark();
        proposedMark.setTitle(title);
        proposedMark.setDescription(Optional.ofNullable(description).orElse(""));
        proposedMark.setMediaFile(proposal.getOriginalMediaFile());

        ProposedMark savedProposedMark = proposedMarkRepository.save(proposedMark);
        proposal.setProposedMark(savedProposedMark);

        proposal.setStatus(ProposalStatus.READY_TO_SUBMIT);
        return proposalRepository.save(proposal);
    }

    private MarkOccurrenceProposal findProposalById(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));
    }

    private void handleGpsData(MarkOccurrenceProposal proposal, Location gpsData) {
        log.info("GPS data found for proposal {}: Latitude={}, Longitude={}", proposal.getId(), gpsData.getLatitude(), gpsData.getLongitude());
        List<Monument> monuments = monumentRepository.findByCoordinatesInRange(
                gpsData.getLatitude(),
                gpsData.getLongitude(),
                COORDINATE_SEARCH_RANGE
        );
        if (!monuments.isEmpty()) {
            log.info("Existing monument found for proposal {} with ID: {}", proposal.getId(), monuments.getFirst().getId());
            proposal.setExistingMonument(monuments.getFirst());
            // Status will be set later based on mark search results
        } else {
            log.info("No existing monument found near GPS coordinates for proposal {}", proposal.getId());
            // Status will be set later based on mark search results
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
