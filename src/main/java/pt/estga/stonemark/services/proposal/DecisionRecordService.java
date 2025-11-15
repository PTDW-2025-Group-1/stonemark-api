package pt.estga.stonemark.services.proposal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.stonemark.entities.proposals.BaseProposal;
import pt.estga.stonemark.entities.proposals.DecisionRecord;

import java.util.List;
import java.util.Optional;

public interface DecisionRecordService {

    List<DecisionRecord> findAllByProposal(BaseProposal proposal);

    Optional<DecisionRecord> findById(Long id);

    DecisionRecord create(DecisionRecord decision);

    void deleteById(Long id);

}
