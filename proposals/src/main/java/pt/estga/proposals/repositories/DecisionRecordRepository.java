package pt.estga.proposals.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.stonemark.entities.proposals.BaseProposal;
import pt.estga.stonemark.entities.proposals.DecisionRecord;

import java.util.List;

@Repository
public interface DecisionRecordRepository extends JpaRepository<DecisionRecord, Long> {

    List<DecisionRecord> findAllByBaseProposal(BaseProposal proposal);

}
