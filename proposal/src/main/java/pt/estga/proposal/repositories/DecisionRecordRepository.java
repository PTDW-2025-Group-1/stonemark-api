package pt.estga.proposal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.proposal.entities.DecisionRecord;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

import java.util.List;

@Repository
public interface DecisionRecordRepository extends JpaRepository<DecisionRecord, Long> {

    List<DecisionRecord> findAllByProposal(MarkOccurrenceProposal proposal);

}
