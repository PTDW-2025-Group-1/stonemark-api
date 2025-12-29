package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MarkOccurrenceRepository;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.content.repositories.MonumentRepository;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposedMonument;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceProposalManagementServiceImpl implements MarkOccurrenceProposalManagementService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final MonumentRepository monumentRepository;
    private final MarkRepository markRepository;
    private final MarkOccurrenceRepository markOccurrenceRepository;
    private final UserService userService;

    // Todo: should create decision record
    // decision record should have creation listener and some procedures
    @Override
    @Transactional
    public void approve(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        validateProposalForApproval(proposal);

        Monument monument = resolveMonument(proposal);
        Mark mark = resolveMark(proposal);
        User proposer = Optional.ofNullable(proposal.getSubmittedById())
                .flatMap(userService::findById)
                .orElse(null);

        MarkOccurrence occurrence = MarkOccurrence.builder()
                .monument(monument)
                .mark(mark)
                .cover(proposal.getOriginalMediaFile())
                .embedding(proposal.getEmbedding())
                .proposer(proposer)
                .build();
        markOccurrenceRepository.save(occurrence);

        proposal.setStatus(ProposalStatus.APPROVED);
        proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void reject(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setStatus(ProposalStatus.REJECTED);
        proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void pending(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setStatus(ProposalStatus.PENDING);
        proposalRepository.save(proposal);
    }

    private MarkOccurrenceProposal findProposalById(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));
    }

    private void validateProposalForApproval(MarkOccurrenceProposal proposal) {
        if (!proposal.isSubmitted()) {
            throw new IllegalStateException("Only submitted proposals can be approved.");
        }
        if (proposal.getExistingMonument() == null && proposal.getProposedMonument() == null) {
            throw new IllegalStateException("Proposal must have either an existing monument or a proposed monument.");
        }
        if (proposal.getExistingMark() == null && !proposal.isNewMark()) {
            throw new IllegalStateException("Proposal must have either an existing mark or be a new mark.");
        }
    }

    private Monument resolveMonument(MarkOccurrenceProposal proposal) {
        // If the proposal links to an existing monument, use it.
        if (proposal.getExistingMonument() != null) {
            return proposal.getExistingMonument();
        }

        // Otherwise, create a new one based on the proposal details.
        // We do NOT automatically check for nearby monuments here to avoid false positives.
        ProposedMonument proposedMonument = proposal.getProposedMonument();
        Monument newMonument = Monument.builder()
                .name(proposedMonument.getName())
                .latitude(proposedMonument.getLatitude())
                .longitude(proposedMonument.getLongitude())
                .build();
        return monumentRepository.save(newMonument);
    }

    private Mark resolveMark(MarkOccurrenceProposal proposal) {
        if (proposal.getExistingMark() != null) {
            return proposal.getExistingMark();
        }

        Mark newMark = Mark.builder()
                .description(proposal.getUserNotes())
                .embedding(proposal.getEmbedding())
                .cover(proposal.getOriginalMediaFile())
                .build();
        return markRepository.save(newMark);
    }
}
