package pt.estga.proposal.services;

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
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposedMonument;
import pt.estga.proposal.enums.SubmissionSource;
import pt.estga.proposal.repositories.ProposedMonumentRepository;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarkOccurrenceProposalChatbotFlowServiceImpl implements MarkOccurrenceProposalChatbotFlowService {

    private final MarkOccurrenceProposalService proposalService;
    private final MediaService mediaService;
    private final MonumentService monumentService;
    private final MarkService markService;
    private final ProposedMonumentRepository proposedMonumentRepository;
    private final DetectionService detectionService;
    private final MarkSearchService markSearchService;
    private final UserService userService;

    private static final double COORDINATE_SEARCH_RANGE = 0.01;

    @Override
    @Transactional
    public MarkOccurrenceProposal startProposal(Long userId) {
        log.info("Starting new chatbot proposal for user ID: {}", userId);
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setSubmissionSource(SubmissionSource.TELEGRAM_BOT);
        if (userId != null) {
            User user = userService.findById(userId).orElseThrow();
            proposal.setSubmittedById(user.getId());
        }
        return proposalService.create(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal addPhoto(Long proposalId, byte[] photoData, String filename) throws IOException {
        log.info("Adding photo to proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        MediaFile mediaFile = mediaService.save(new ByteArrayInputStream(photoData), filename);
        proposal.setOriginalMediaFile(mediaFile);
        return proposalService.update(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal addLocation(Long proposalId, Double latitude, Double longitude) {
        log.info("Adding location to proposal ID: {}. Lat: {}, Lon: {}", proposalId, latitude, longitude);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setLatitude(latitude);
        proposal.setLongitude(longitude);
        return proposalService.update(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal analyzePhoto(Long proposalId) throws IOException {
        log.info("Analyzing photo for proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        if (proposal.getOriginalMediaFile() == null) {
            log.warn("No photo found for proposal ID: {}", proposalId);
            return proposal;
        }

        Resource photoResource = mediaService.loadFile(proposal.getOriginalMediaFile().getStoragePath());

        try (InputStream is = photoResource.getInputStream()) {
            byte[] photoData = is.readAllBytes();
            try (ByteArrayInputStream detectionInputStream = new ByteArrayInputStream(photoData)) {
                DetectionResult detectionResult = detectionService.detect(detectionInputStream, proposal.getOriginalMediaFile().getFileName());
                if (detectionResult != null && detectionResult.embedding() != null && !detectionResult.embedding().isEmpty()) {
                    List<Double> embeddedVector = detectionResult.embedding();
                    proposal.setEmbedding(embeddedVector);
                } else {
                    log.info("No embedding detected for proposal {}", proposal.getId());
                }
            }
        } catch (Exception e) {
            log.warn("Detection service failed for proposal ID: {}. Proceeding without detection.", proposalId, e);
        }

        return proposalService.update(proposal);
    }

    @Override
    public List<String> getSuggestedMonumentIds(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
            List<Monument> monuments = monumentService.findByCoordinatesInRange(
                    proposal.getLatitude(), proposal.getLongitude(), COORDINATE_SEARCH_RANGE
            );
            return monuments.stream()
                    .map(m -> m.getId().toString())
                    .toList();
        }
        return List.of();
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal selectMonument(Long proposalId, Long monumentId) {
        log.info("Selecting monument ID: {} for proposal ID: {}", monumentId, proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        clearMonumentSelections(proposal);

        monumentService.findById(monumentId)
                .ifPresentOrElse(
                        proposal::setExistingMonument,
                        () -> {
                            log.error("Monument ID {} not found", monumentId);
                            throw new RuntimeException("Monument not found");
                        }
                );

        return proposalService.update(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal createMonument(Long proposalId, String name) {
        log.info("Creating new monument '{}' for proposal ID: {}", name, proposalId);
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
    public List<String> getSuggestedMarkIds(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        if (proposal.getEmbedding() != null && !proposal.getEmbedding().isEmpty()) {
            try {
                return markSearchService.searchMarks(proposal.getEmbedding());
            } catch (Exception e) {
                log.warn("Mark search service failed for proposal ID: {}. Proceeding without suggestions.", proposalId, e);
            }
        }
        return List.of();
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal selectMark(Long proposalId, Long markId) {
        log.info("Selecting mark ID: {} for proposal ID: {}", markId, proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        clearMarkSelections(proposal);

        markService.findById(markId).ifPresentOrElse(
                proposal::setExistingMark,
                () -> {
                    log.error("Mark ID {} not found", markId);
                    throw new RuntimeException("Mark not found");
                }
        );

        return proposalService.update(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal createMark(Long proposalId, String description) {
        log.info("Creating new mark for proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        clearMarkSelections(proposal);

        proposal.setNewMark(true);
        if (description != null) {
            proposal.setUserNotes(description);
        }

        return proposalService.update(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal addNotes(Long proposalId, String notes) {
        log.info("Adding notes to proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setUserNotes(notes);
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

    private void clearMonumentSelections(MarkOccurrenceProposal proposal) {
        proposal.setExistingMonument(null);
        if (proposal.getProposedMonument() != null) {
            proposedMonumentRepository.delete(proposal.getProposedMonument());
            proposal.setProposedMonument(null);
        }
    }

    private void clearMarkSelections(MarkOccurrenceProposal proposal) {
        proposal.setExistingMark(null);
        proposal.setNewMark(false);
    }
}
