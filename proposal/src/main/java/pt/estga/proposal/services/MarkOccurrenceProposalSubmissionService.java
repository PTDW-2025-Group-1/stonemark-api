package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.file.entities.MediaFile;
import pt.estga.proposal.dtos.MarkOccurrenceProposalCreateDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.events.ProposalSubmittedEvent;
import pt.estga.user.entities.User;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarkOccurrenceProposalSubmissionService {

    private final MarkOccurrenceProposalService proposalService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MarkOccurrenceProposal submit(MarkOccurrenceProposal proposal) {
        log.info("Submitting proposal");

        if (ProposalStatus.SUBMITTED.equals(proposal.getStatus())) {
            log.warn("Proposal is already submitted. Skipping submission logic.");
            return proposal;
        }

        proposal.setSubmittedAt(Instant.now());
        proposal.setStatus(ProposalStatus.SUBMITTED);

        MarkOccurrenceProposal savedProposal = proposalService.create(proposal);
        log.info("Proposal submitted successfully with ID: {}", savedProposal.getId());
        
        eventPublisher.publishEvent(new ProposalSubmittedEvent(this, savedProposal.getId()));
        log.debug("Published ProposalSubmittedEvent for proposal ID: {}", savedProposal.getId());

        return savedProposal;
    }

    @Transactional
    public MarkOccurrenceProposal createAndSubmit(MarkOccurrenceProposalCreateDto dto, User user) {
        log.info("Creating and submitting new proposal for user: {}", user.getId());

        MarkOccurrenceProposal proposal = MarkOccurrenceProposal.builder()
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .userNotes(dto.userNotes())
                .monumentName(dto.monumentName())
                .submissionSource(dto.submissionSource())
                .submittedBy(user)
                .submittedAt(Instant.now())
                .status(ProposalStatus.SUBMITTED)
                .newMark(true) // Default to true, logic might change if existingMarkId is present
                .build();

        if (dto.photoId() != null) {
            proposal.setOriginalMediaFile(MediaFile.builder().id(dto.photoId()).build());
        }

        if (dto.existingMonumentId() != null) {
            proposal.setExistingMonument(Monument.builder().id(dto.existingMonumentId()).build());
        }

        if (dto.existingMarkId() != null) {
            proposal.setExistingMark(Mark.builder().id(dto.existingMarkId()).build());
            proposal.setNewMark(false);
        }

        MarkOccurrenceProposal savedProposal = proposalService.create(proposal);
        log.info("Proposal created and submitted with ID: {}", savedProposal.getId());

        eventPublisher.publishEvent(new ProposalSubmittedEvent(this, savedProposal.getId()));

        return savedProposal;
    }
}
