package pt.estga.proposals.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.proposals.entities.BaseProposal;
import pt.estga.proposals.entities.DecisionRecord;
import pt.estga.proposals.repositories.DecisionRecordRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DecisionRecordServiceHibernateImpl implements DecisionRecordService {

    private final DecisionRecordRepository repository;

    @Override
    public List<DecisionRecord> findAllByProposal(BaseProposal proposal) {
        return repository.findAllByBaseProposal(proposal);
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
