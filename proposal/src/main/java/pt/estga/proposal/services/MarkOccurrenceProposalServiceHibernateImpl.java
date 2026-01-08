package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.dtos.MarkOccurrenceProposalStatsDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.user.entities.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceProposalServiceHibernateImpl implements MarkOccurrenceProposalService {

    private final MarkOccurrenceProposalRepository repository;

    @Override
    public Page<MarkOccurrenceProposal> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<MarkOccurrenceProposal> findById(Long id) {
        return repository.findByIdDetailed(id);
    }

    @Override
    public Page<MarkOccurrenceProposal> findByUser(User user, Pageable pageable) {
        return repository.findBySubmittedById(user.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MarkOccurrenceProposal> findIncompleteByUserId(Long userId) {
        return repository.findFirstBySubmitted(false);
    }

    @Override
    public MarkOccurrenceProposal create(MarkOccurrenceProposal proposal) {
        return repository.save(proposal);
    }

    @Override
    public MarkOccurrenceProposal update(MarkOccurrenceProposal proposal) {
        return repository.save(proposal);
    }

    @Override
    public void delete(MarkOccurrenceProposal proposal) {
        repository.delete(proposal);
    }

    @Override
    public MarkOccurrenceProposalStatsDto getStatsByUser(User user) {
        return repository.getStatsByUserId(user.getId());
    }

    @Override
    public long countApprovedProposalsByUserId(Long userId) {
        return repository.countBySubmittedByIdAndStatusIn(userId,
                List.of(ProposalStatus.AUTO_ACCEPTED, ProposalStatus.MANUALLY_ACCEPTED));
    }

}
