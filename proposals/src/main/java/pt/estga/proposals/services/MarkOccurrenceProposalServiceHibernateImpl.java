package pt.estga.proposals.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;
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
        return repository.findById(id);
    }

    @Override
    public List<MarkOccurrenceProposal> findByUser(User user) {
        return repository.findByCreatedBy(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MarkOccurrenceProposal> findIncompleteByUserId(Long userId) {
        return repository.findFirstByIsSubmitted(false);
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
    public long countApprovedProposalsByUser(User user) {
        return repository.countApprovedProposalsByUser(user);
    }

}
