package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ProposalAdminListDto;
import pt.estga.proposal.dtos.ProposalFilter;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.mappers.ProposalAdminMapper;
import pt.estga.proposal.projections.MarkOccurrenceProposalStatsProjection;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;
import pt.estga.shared.exceptions.ResourceNotFoundException;
import pt.estga.user.entities.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarkOccurrenceProposalServiceHibernateImpl implements MarkOccurrenceProposalService {

    private final MarkOccurrenceProposalRepository repository;
    private final ProposalDecisionAttemptRepository decisionRepository;
    private final ProposalAdminMapper adminMapper;

    @Override
    public Page<MarkOccurrenceProposal> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<MarkOccurrenceProposal> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<MarkOccurrenceProposal> findByUser(User user, Pageable pageable) {
        return repository.findBySubmittedBy(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MarkOccurrenceProposal> findByIdWithRelations(Long id) {
        return repository.findByIdWithRelations(id);
    }

    @Override
    public MarkOccurrenceProposal create(MarkOccurrenceProposal proposal) {
        return repository.save(proposal);
    }

    @Override
    public MarkOccurrenceProposal update(MarkOccurrenceProposal proposal) {
        return repository.save(proposal);
    }

    @Override
    public void delete(MarkOccurrenceProposal proposal) {
        repository.delete(proposal);
    }

    @Override
    public MarkOccurrenceProposalStatsProjection getStatsByUser(User user) {
        return repository.getStatsByUserId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProposalAdminListDto> getAdminProposals(ProposalFilter filter, Pageable pageable) {
        Collection<ProposalStatus> statuses = filter.statuses();

        if (statuses != null && statuses.isEmpty()) {
            statuses = null;
        }
        
        return repository.findByFilters(statuses, filter.submittedById(), pageable)
                .map(adminMapper::toAdminListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProposalModeratorViewDto getAdminProposalDetails(Long id) {
        MarkOccurrenceProposal proposal = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Proposal with ID {} not found", id);
                    return new ResourceNotFoundException("Proposal not found with id: " + id);
                });
        return adminMapper.toModeratorViewDto(proposal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecisionHistoryItem> getDecisionHistory(Long proposalId) {
        return adminMapper.toDecisionHistoryList(
                decisionRepository.findByProposalIdOrderByDecidedAtDesc(proposalId)
        );
    }
}
