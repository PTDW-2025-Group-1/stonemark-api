package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.file.services.MediaService;
import pt.estga.proposal.events.ProposalPhotoUploadedEvent;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProposalEnrichmentListener {

    private final MarkOccurrenceProposalRepository proposalRepo;
    private final DetectionService detectionService;
    private final MediaService mediaService;

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
