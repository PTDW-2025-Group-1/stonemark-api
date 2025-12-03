package pt.estga.proposals.services;

import pt.estga.proposals.entities.DecisionRecord;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.List;
import java.util.Optional;

public interface DecisionRecordService {

    List<DecisionRecord> findAllByProposal(MarkOccurrenceProposal proposal);

    Optional<DecisionRecord> findById(Long id);

    DecisionRecord create(DecisionRecord decision);

    void deleteById(Long id);

}
