package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MarkService;
import pt.estga.content.services.MonumentService;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.detection.service.MarkSearchService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.SubmissionSource;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarkOccurrenceProposalChatbotFlowServiceImpl implements MarkOccurrenceProposalChatbotFlowService {

    private final MarkOccurrenceProposalService proposalService;
    private final MediaService mediaService;
    private final MonumentService monumentService;
    private final MarkService markService;
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
    public void addPhotoAndAnalyze(Long proposalId, byte[] photoData, String filename) throws IOException {
        log.info("Adding and analyzing photo for proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        MediaFile mediaFile = mediaService.save(new ByteArrayInputStream(photoData), filename);
        proposal.setOriginalMediaFile(mediaFile);

        try (ByteArrayInputStream detectionInputStream = new ByteArrayInputStream(photoData)) {
            DetectionResult detectionResult = detectionService.detect(detectionInputStream, filename);
            if (detectionResult != null && detectionResult.embedding() != null && !detectionResult.embedding().isEmpty()) {
                List<Double> embeddedVector = detectionResult.embedding();
                proposal.setEmbedding(embeddedVector);
            } else {
                log.info("No embedding detected for proposal {}", proposal.getId());
            }
        } catch (Exception e) {
            log.warn("Detection service failed for proposal ID: {}. Proceeding without detection.", proposalId, e);
        }

        proposalService.update(proposal);
    }

    @Override
    @Transactional
    public void addLocation(Long proposalId, Double latitude, Double longitude) {
        log.info("Adding location to proposal ID: {}. Lat: {}, Lon: {}", proposalId, latitude, longitude);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setLatitude(latitude);
        proposal.setLongitude(longitude);
        proposalService.update(proposal);
    }

    @Override
    public List<Monument> suggestMonuments(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
            return monumentService.findByCoordinatesInRange(
                    proposal.getLatitude(), proposal.getLongitude(), COORDINATE_SEARCH_RANGE
            );
        }
        return List.of();
    }

    @Override
    @Transactional
    public void selectMonument(Long proposalId, Long monumentId) {
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

        proposalService.update(proposal);
    }

    @Override
    @Transactional
    public void setNewMonumentName(Long proposalId, String name) {
        log.info("Setting new monument name '{}' for proposal ID: {}", name, proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        clearMonumentSelections(proposal);

        proposal.setMonumentName(name);

        proposalService.update(proposal);
    }

    @Override
    public List<Mark> suggestMarks(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        if (proposal.getEmbedding() != null && !proposal.getEmbedding().isEmpty()) {
            try {
                List<String> markIds = markSearchService.searchMarks(proposal.getEmbedding());
                return markIds.stream()
                        .map(Long::valueOf)
                        .map(markService::findById)
                        .filter(java.util.Optional::isPresent)
                        .map(java.util.Optional::get)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("Mark search service failed for proposal ID: {}. Proceeding without suggestions.", proposalId, e);
            }
        }
        return List.of();
    }

    @Override
    @Transactional
    public void selectMark(Long proposalId, Long markId) {
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

        proposalService.update(proposal);
    }

    @Override
    @Transactional
    public void indicateNewMark(Long proposalId) {
        log.info("Setting new mark for proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        clearMarkSelections(proposal);

        proposal.setNewMark(true);

        proposalService.update(proposal);
    }

    @Override
    @Transactional
    public void addNotes(Long proposalId, String notes) {
        log.info("Adding notes to proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setUserNotes(notes);
        proposalService.update(proposal);
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
        proposal.setMonumentName(null);
    }

    private void clearMarkSelections(MarkOccurrenceProposal proposal) {
        proposal.setExistingMark(null);
        proposal.setNewMark(false);
    }
}
