package pt.estga.proposal.services;

import pt.estga.proposal.entities.DecisionRecord;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

import java.util.List;
import java.util.Optional;

public interface DecisionRecordService {

    List<DecisionRecord> findAllByProposal(MarkOccurrenceProposal proposal);

    Optional<DecisionRecord> findById(Long id);

    DecisionRecord create(DecisionRecord decision);

    void deleteById(Long id);

}
