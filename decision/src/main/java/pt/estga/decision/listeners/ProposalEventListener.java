package pt.estga.decision.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MonumentService;
import pt.estga.decision.services.ProposalDecisionService;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.file.services.MediaService;
import pt.estga.proposal.events.ProposalAcceptedEvent;
import pt.estga.proposal.events.ProposalPhotoUploadedEvent;
import pt.estga.proposal.events.ProposalSubmittedEvent;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProposalEventListener {

    private final ProposalDecisionService proposalDecisionService;
    private final MarkOccurrenceProposalRepository proposalRepo;
    private final MonumentService monumentService;
    private final DetectionService detectionService;
    private final MediaService mediaService;

    @Async
    @EventListener
    @Transactional
    public void handleProposalSubmitted(ProposalSubmittedEvent event) {
        Long proposalId = event.getProposal().getId();
        log.debug("Async processing of submitted proposal ID: {}", proposalId);

        proposalRepo.findById(proposalId).ifPresentOrElse(
                proposalDecisionService::makeAutomaticDecision,
                () -> log.error("Proposal with ID {} not found during async processing", proposalId)
        );
    }

    @Async
    @EventListener
    @Transactional
    public void handleProposalAccepted(ProposalAcceptedEvent event) {
        Long proposalId = event.getProposal().getId();
        log.debug("Async processing of accepted proposal ID: {}", proposalId);

        proposalRepo.findById(proposalId).ifPresent(proposal -> {
            if (proposal.getExistingMonument() != null && !proposal.getExistingMonument().getActive()) {
                Monument monument = proposal.getExistingMonument();
                monument.setActive(true);
                monumentService.update(monument);
                log.info("Activated monument ID: {}", monument.getId());
            }
        });
    }

    @Async
    @EventListener
    @Transactional
    public void handleProposalPhotoUploaded(ProposalPhotoUploadedEvent event) {
        Long proposalId = event.getProposal().getId();
        log.info("Async detection processing for proposal ID: {}", proposalId);

        proposalRepo.findById(proposalId).ifPresent(proposal -> {
            try (InputStream detectionInputStream = mediaService.loadFileById(proposal.getOriginalMediaFile().getId()).getInputStream()) {
                DetectionResult detectionResult = detectionService.detect(detectionInputStream, proposal.getOriginalMediaFile().getOriginalFilename());
                if (detectionResult != null && detectionResult.embedding() != null && !detectionResult.embedding().isEmpty()) {
                    double[] embeddedVector = detectionResult.embedding().stream().mapToDouble(Double::doubleValue).toArray();
                    proposal.setEmbedding(embeddedVector);
                    proposalRepo.save(proposal);
                    log.info("Successfully updated embedding for proposal ID: {}", proposalId);
                } else {
                    log.info("No embedding detected for proposal {}", proposalId);
                }
            } catch (Exception e) {
                log.warn("Detection service failed for proposal ID: {}. Proceeding without detection.", proposalId, e);
            }
        });
    }
}
