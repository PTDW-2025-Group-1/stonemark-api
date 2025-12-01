package pt.estga.proposals.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.FileStorageService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ProposalSubmissionServiceImpl implements ProposalSubmissionService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final DetectionService detectionService;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public MarkOccurrenceProposal submit(Long proposalId) {
        MarkOccurrenceProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        MediaFile mediaFile = proposal.getOriginalMediaFile();
        Resource resource = fileStorageService.loadFile(mediaFile.getStoragePath());

        try (InputStream inputStream = resource.getInputStream()) {
            DetectionResult detectionResult = detectionService.detect(inputStream);

            if (detectionResult.isMasonMark()) {
                proposal.setStatus(ProposalStatus.SUBMITTED);
                // In the future, we can store the vector in the proposal or a related entity
                // proposal.setVector(detectionResult.getVector());
            } else {
                proposal.setStatus(ProposalStatus.REJECTED);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading image data", e);
        }

        return proposalRepository.save(proposal);
    }
}
