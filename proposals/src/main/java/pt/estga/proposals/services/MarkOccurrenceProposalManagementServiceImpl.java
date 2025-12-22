package pt.estga.proposals.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MarkOccurrenceRepository;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.content.repositories.MonumentRepository;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.entities.ProposedMark;
import pt.estga.proposals.entities.ProposedMonument;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceProposalManagementServiceImpl implements MarkOccurrenceProposalManagementService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final MonumentRepository monumentRepository;
    private final MarkRepository markRepository;
    private final MarkOccurrenceRepository markOccurrenceRepository;

    @Override
    @Transactional
    public MarkOccurrenceProposal approve(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        validateProposalForApproval(proposal);

        Monument monument = resolveMonument(proposal);
        Mark mark = resolveMark(proposal);

        MarkOccurrence occurrence = MarkOccurrence.builder()
                .monument(monument)
                .mark(mark)
                .image(proposal.getOriginalMediaFile())
                .embedding(proposal.getEmbedding())
                .build();
        markOccurrenceRepository.save(occurrence);

        proposal.setStatus(ProposalStatus.APPROVED);
        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal reject(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setStatus(ProposalStatus.REJECTED);
        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal pending(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);
        proposal.setStatus(ProposalStatus.PENDING);
        return proposalRepository.save(proposal);
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
        if (proposal.getExistingMark() == null && proposal.getProposedMark() == null) {
            throw new IllegalStateException("Proposal must have either an existing mark or a proposed mark.");
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

        ProposedMark proposedMark = proposal.getProposedMark();
        Mark newMark = Mark.builder()
                .description(proposedMark.getDescription())
                .embedding(proposal.getEmbedding())
                .cover(proposedMark.getMediaFile())
                .build();
        return markRepository.save(newMark);
    }
}
