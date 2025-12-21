package pt.estga.proposals.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.FileStorageService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.entities.ProposedMark;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.events.ProposalSubmittedEvent;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceProposalSubmissionServiceImpl implements MarkOccurrenceProposalSubmissionService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final DetectionService detectionService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MarkOccurrenceProposal submit(Long proposalId) {
        MarkOccurrenceProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        MediaFile mediaFile = proposal.getOriginalMediaFile();
        Resource resource = fileStorageService.loadFile(mediaFile.getStoragePath());

        try (InputStream inputStream = resource.getInputStream()) {
            DetectionResult detectionResult = detectionService.detect(inputStream, mediaFile.getFileName());

            proposal.setStatus(ProposalStatus.SUBMITTED);

            // Save embedding for the MarkOccurrenceProposal itself
            if (detectionResult != null && detectionResult.embedding() != null) {
                proposal.setEmbedding(detectionResult.embedding());
            }

            // If there's a proposed mark, save its embedding too (if applicable)
            ProposedMark proposedMark = proposal.getProposedMark();
            if (proposedMark != null && detectionResult != null && detectionResult.embedding() != null) {
                proposedMark.setEmbedding(detectionResult.embedding());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading image data", e);
        }

        MarkOccurrenceProposal savedProposal = proposalRepository.save(proposal);
        eventPublisher.publishEvent(new ProposalSubmittedEvent(this, savedProposal));

        return savedProposal;
    }
}
