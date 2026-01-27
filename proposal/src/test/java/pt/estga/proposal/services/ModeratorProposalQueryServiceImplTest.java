package pt.estga.proposal.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;
import pt.estga.shared.exceptions.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModeratorProposalQueryServiceImplTest {

    @Mock
    private MarkOccurrenceProposalRepository proposalRepository;

    @Mock
    private ProposalDecisionAttemptRepository decisionRepository;

    @InjectMocks
    private ProposalAdminQueryServiceImpl queryService;

    @Test
    void getProposal_ShouldReturnDto_WhenProposalExists() {
        // Arrange
        Long proposalId = 1L;
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);
        proposal.setStatus(ProposalStatus.SUBMITTED);
        proposal.setPriority(100);
        proposal.setSubmittedAt(Instant.now());
        proposal.setMonumentName("Test Monument");

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));

        // Act
        ProposalModeratorViewDto result = queryService.getProposal(proposalId);

        // Assert
        assertNotNull(result);
        assertEquals(proposalId, result.id());
        assertEquals(ProposalStatus.SUBMITTED, result.status());
        assertEquals(100, result.priority());
        assertEquals("Test Monument", result.monumentName());
        assertNull(result.activeDecision());
    }

    @Test
    void getProposal_ShouldReturnDtoWithActiveDecision_WhenActiveDecisionExists() {
        // Arrange
        Long proposalId = 1L;
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);

        ProposalDecisionAttempt decision = new ProposalDecisionAttempt();
        decision.setId(10L);
        decision.setType(DecisionType.AUTOMATIC);
        decision.setOutcome(DecisionOutcome.ACCEPT);
        decision.setConfident(true);
        decision.setDecidedAt(Instant.now());

        proposal.setActiveDecision(decision);

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));

        // Act
        ProposalModeratorViewDto result = queryService.getProposal(proposalId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.activeDecision());
        assertEquals(1L, result.activeDecision().id());
        assertEquals(DecisionType.AUTOMATIC, result.activeDecision().type());
        assertEquals(DecisionOutcome.ACCEPT, result.activeDecision().outcome());
    }

    @Test
    void getProposal_ShouldThrowException_WhenProposalNotFound() {
        // Arrange
        Long proposalId = 999L;
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> queryService.getProposal(proposalId));
    }

    @Test
    void getDecisionHistory_ShouldReturnList_WhenHistoryExists() {
        // Arrange
        Long proposalId = 1L;
        ProposalDecisionAttempt decision1 = new ProposalDecisionAttempt();
        decision1.setId(1L);
        decision1.setDecidedAt(Instant.now());

        ProposalDecisionAttempt decision2 = new ProposalDecisionAttempt();
        decision2.setId(2L);
        decision2.setDecidedAt(Instant.now().minusSeconds(60));

        when(decisionRepository.findByProposalIdOrderByDecidedAtDesc(proposalId))
                .thenReturn(List.of(decision1, decision2));

        // Act
        List<DecisionHistoryItem> result = queryService.getDecisionHistory(proposalId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
    }

    @Test
    void getDecisionHistory_ShouldReturnEmptyList_WhenNoHistory() {
        // Arrange
        Long proposalId = 1L;
        when(decisionRepository.findByProposalIdOrderByDecidedAtDesc(proposalId))
                .thenReturn(List.of());

        // Act
        List<DecisionHistoryItem> result = queryService.getDecisionHistory(proposalId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
