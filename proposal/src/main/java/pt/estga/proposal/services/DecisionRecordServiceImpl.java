package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.proposal.entities.DecisionRecord;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.repositories.DecisionRecordRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DecisionRecordServiceImpl implements DecisionRecordService {

    private final DecisionRecordRepository repository;

    @Override
    public List<DecisionRecord> findAllByProposal(MarkOccurrenceProposal proposal) {
        return repository.findAllByProposal(proposal);
    }

    @Override
    public Optional<DecisionRecord> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public DecisionRecord create(DecisionRecord decision) {
        return repository.save(decision);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
