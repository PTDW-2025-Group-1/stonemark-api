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
public class ProposalManagementServiceImpl implements ProposalManagementService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final MonumentRepository monumentRepository;
    private final MarkRepository markRepository;
    private final MarkOccurrenceRepository markOccurrenceRepository;

    @Override
    @Transactional
    public MarkOccurrenceProposal approve(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted proposals can be approved.");
        }

        Monument monument = proposal.getExistingMonument();
        if (monument == null) {
            ProposedMonument proposedMonument = proposal.getProposedMonument();
            monument = new Monument();
            monument.setName(proposedMonument.getName());
            monument.setLatitude(proposedMonument.getLatitude());
            monument.setLongitude(proposedMonument.getLongitude());
            monument = monumentRepository.save(monument);
        }

        Mark mark = proposal.getExistingMark();
        if (mark == null) {
            ProposedMark proposedMark = proposal.getProposedMark();
            mark = new Mark();
            mark.setTitle(proposedMark.getTitle());
            mark.setDescription(proposedMark.getDescription());
            mark = markRepository.save(mark);
        }

        MarkOccurrence occurrence = new MarkOccurrence();
        occurrence.setMonument(monument);
        occurrence.setMark(mark);
        occurrence.setImage(proposal.getOriginalMediaFile());
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

    private MarkOccurrenceProposal findProposalById(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));
    }
}
