package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MarkSearchService;
import pt.estga.content.services.MarkService;
import pt.estga.content.services.MonumentService;
import pt.estga.detection.service.DetectionService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.SubmissionSource;
import pt.estga.proposal.events.ProposalPhotoUploadedEvent;
import pt.estga.user.entities.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarkOccurrenceProposalChatbotFlowServiceImpl implements MarkOccurrenceProposalChatbotFlowService {

    private final MediaService mediaService;
    private final MonumentService monumentService;
    private final MarkService markService;
    private final DetectionService detectionService;
    private final MarkSearchService markSearchService;
    private final ApplicationEventPublisher eventPublisher;

    private static final double COORDINATE_SEARCH_RANGE = 0.01;

    @Override
    @Transactional
    public MarkOccurrenceProposal startProposal(User user) {
        log.info("Starting new chatbot proposal for user ID: {}", user.getId());
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setSubmissionSource(SubmissionSource.TELEGRAM_BOT);
        proposal.setSubmittedBy(user);
        return proposal;
    }

    @Override
    @Transactional
    public void addPhoto(MarkOccurrenceProposal proposal, byte[] photoData, String filename) throws IOException {
        log.info("Adding photo for proposal");

        MediaFile mediaFile = mediaService.save(new ByteArrayInputStream(photoData), filename);
        proposal.setOriginalMediaFile(mediaFile);
        
        // Publish event for async processing (e.g., detection)
        eventPublisher.publishEvent(new ProposalPhotoUploadedEvent(this, proposal));
    }

    @Override
    @Transactional
    public void addLocation(MarkOccurrenceProposal proposal, Double latitude, Double longitude) {
        log.info("Adding location to proposal. Lat: {}, Lon: {}", latitude, longitude);
        proposal.setLatitude(latitude);
        proposal.setLongitude(longitude);
    }

    @Override
    public List<Monument> suggestMonuments(MarkOccurrenceProposal proposal) {
        if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
            return monumentService.findByCoordinatesInRange(
                    proposal.getLatitude(), proposal.getLongitude(), COORDINATE_SEARCH_RANGE
            );
        }
        return List.of();
    }

    @Override
    @Transactional
    public void selectMonument(MarkOccurrenceProposal proposal, Long monumentId) {
        log.info("Selecting monument ID: {} for proposal", monumentId);
        clearMonumentSelections(proposal);

        monumentService.findById(monumentId)
                .ifPresentOrElse(
                        proposal::setExistingMonument,
                        () -> {
                            log.error("Monument ID {} not found", monumentId);
                            throw new RuntimeException("Monument not found");
                        }
                );
    }

    @Override
    @Transactional
    public void setNewMonumentName(MarkOccurrenceProposal proposal, String name) {
        log.info("Setting new monument name '{}' for proposal", name);
        clearMonumentSelections(proposal);

        proposal.setMonumentName(name);
    }

    @Override
    public List<Mark> suggestMarks(MarkOccurrenceProposal proposal) {
        if (proposal.getEmbedding() != null && proposal.getEmbedding().length > 0) {
            try {
                List<String> markIds = markSearchService.searchMarks(proposal.getEmbedding());
                return markIds.stream()
                        .map(Long::valueOf)
                        .map(markService::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("Mark search service failed for proposal. Proceeding without suggestions.", e);
            }
        }
        return List.of();
    }

    @Override
    @Transactional
    public void selectMark(MarkOccurrenceProposal proposal, Long markId) {
        log.info("Selecting mark ID: {} for proposal", markId);
        clearMarkSelections(proposal);

        markService.findById(markId).ifPresentOrElse(
                proposal::setExistingMark,
                () -> {
                    log.error("Mark ID {} not found", markId);
                    throw new RuntimeException("Mark not found");
                }
        );
    }

    @Override
    @Transactional
    public void indicateNewMark(MarkOccurrenceProposal proposal) {
        log.info("Setting new mark for proposal");
        clearMarkSelections(proposal);

        proposal.setNewMark(true);
    }

    @Override
    @Transactional
    public void addNotes(MarkOccurrenceProposal proposal, String notes) {
        log.info("Adding notes to proposal");
        proposal.setUserNotes(notes);
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
