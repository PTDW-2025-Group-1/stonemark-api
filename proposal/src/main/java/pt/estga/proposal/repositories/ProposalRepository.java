package pt.estga.proposal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import pt.estga.proposal.entities.Proposal;

@NoRepositoryBean
public interface ProposalRepository<T extends Proposal> extends JpaRepository<T, Long> {
}
