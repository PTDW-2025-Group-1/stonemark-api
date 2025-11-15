package pt.estga.stonemark.repositories.proposals;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.stonemark.entities.proposals.BaseProposal;
import pt.estga.stonemark.entities.proposals.DecisionRecord;

import java.util.List;

public interface DecisionRecordRepository extends JpaRepository<DecisionRecord, Long> {

    List<DecisionRecord> findAllByBaseProposal(BaseProposal proposal);

}
