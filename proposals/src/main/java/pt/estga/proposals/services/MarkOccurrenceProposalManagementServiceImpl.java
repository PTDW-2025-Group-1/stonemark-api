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

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceProposalManagementServiceImpl implements MarkOccurrenceProposalManagementService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final MonumentRepository monumentRepository;
    private final MarkRepository markRepository;
    private final MarkOccurrenceRepository markOccurrenceRepository;

    // Search range in decimal degrees (~1.1km at equator)
    private static final double COORDINATE_SEARCH_RANGE = 0.01;

    @Override
    @Transactional
    public MarkOccurrenceProposal approve(Long proposalId) {
        MarkOccurrenceProposal proposal = findProposalById(proposalId);

        if (!proposal.isSubmitted()) {
            throw new IllegalStateException("Only submitted proposals can be approved.");
        }

        Monument monument = proposal.getExistingMonument();
        if (monument == null) {
            ProposedMonument proposedMonument = proposal.getProposedMonument();
            List<Monument> existingMonuments = monumentRepository.findByCoordinatesInRange(
                    proposedMonument.getLatitude(),
                    proposedMonument.getLongitude(),
                    COORDINATE_SEARCH_RANGE
            );

            if (!existingMonuments.isEmpty()) {
                monument = existingMonuments.getFirst();
            } else {
                monument = Monument.builder()
                        .name(proposedMonument.getName())
                        .latitude(proposedMonument.getLatitude())
                        .longitude(proposedMonument.getLongitude())
                        .build();
                monument = monumentRepository.save(monument);
            }
        }

        Mark mark = proposal.getExistingMark();
        if (mark == null) {
            ProposedMark proposedMark = proposal.getProposedMark();
            mark = Mark.builder()
                    .title(proposedMark.getTitle())
                    .description(proposedMark.getDescription())
                    .embedding(proposal.getEmbedding())
                    .cover(proposedMark.getMediaFile())
                    .build();
            mark = markRepository.save(mark);
        }

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
}
