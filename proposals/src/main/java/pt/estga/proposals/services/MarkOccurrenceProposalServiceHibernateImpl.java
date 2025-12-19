package pt.estga.proposals.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;
import pt.estga.user.entities.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceProposalServiceHibernateImpl implements MarkOccurrenceProposalService {

    private final MarkOccurrenceProposalRepository repository;
    private final ObjectMapper objectMapper;

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
    public List<MarkOccurrenceProposal> findByStatus(ProposalStatus status) {
        return repository.findByStatus(status);
    }

    @Override
    public long countApprovedProposalsByUser(User user) {
        return repository.countApprovedProposalsByUser(user);
    }

    @Override
    public MarkOccurrenceProposal save(MarkOccurrenceProposal proposal) {
        return repository.save(proposal);
    }
}
