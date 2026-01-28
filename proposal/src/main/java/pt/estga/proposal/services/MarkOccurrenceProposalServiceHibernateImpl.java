package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.projections.MarkOccurrenceProposalStatsProjection;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.user.entities.User;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarkOccurrenceProposalServiceHibernateImpl implements MarkOccurrenceProposalService {

    private final MarkOccurrenceProposalRepository repository;

    @Override
    public Page<MarkOccurrenceProposal> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Cacheable(value = "proposals", key = "#id")
    public Optional<MarkOccurrenceProposal> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<MarkOccurrenceProposal> findByUser(User user, Pageable pageable) {
        return repository.findBySubmittedBy(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MarkOccurrenceProposal> findByIdWithRelations(Long id) {
        return repository.findByIdWithRelations(id);
    }

    @Override
    @CacheEvict(value = "proposalStats", key = "#proposal.submittedBy?.id")
    public MarkOccurrenceProposal create(MarkOccurrenceProposal proposal) {
        return repository.save(proposal);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "proposals", key = "#proposal.id"),
            @CacheEvict(value = "proposalStats", key = "#proposal.submittedBy?.id")
    })
    public MarkOccurrenceProposal update(MarkOccurrenceProposal proposal) {
        return repository.save(proposal);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "proposals", key = "#proposal.id"),
            @CacheEvict(value = "proposalStats", key = "#proposal.submittedBy?.id")
    })
    public void delete(MarkOccurrenceProposal proposal) {
        repository.delete(proposal);
    }

    @Override
    @Cacheable(value = "proposalStats", key = "#user.id")
    public MarkOccurrenceProposalStatsProjection getStatsByUser(User user) {
        return repository.getStatsByUserId(user.getId());
    }
}
