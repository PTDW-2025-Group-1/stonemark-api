package pt.estga.proposal.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;
import pt.estga.shared.exceptions.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManualDecisionServiceTest {

    @Mock
    private ProposalDecisionAttemptRepository attemptRepo;

    @Mock
    private MarkOccurrenceProposalRepository proposalRepo;

    @InjectMocks
    private ManualDecisionService manualDecisionService;

    @Test
    void createManualDecision_ShouldCreateAttemptAndUpdateProposal_WhenAccepted() {
        // Arrange
        Long proposalId = 1L;
        Long moderatorId = 100L;
        String notes = "Looks good";
        DecisionOutcome outcome = DecisionOutcome.ACCEPT;

        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);
        proposal.setStatus(ProposalStatus.SUBMITTED);

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));

        // Act
        ProposalDecisionAttempt result = manualDecisionService.createManualDecision(proposalId, outcome, notes, moderatorId);

        // Assert
        assertNotNull(result);
        assertEquals(DecisionType.MANUAL, result.getType());
        assertEquals(outcome, result.getOutcome());
        assertEquals(notes, result.getNotes());
        assertEquals(moderatorId, result.getDecidedBy());
        assertTrue(result.getConfident());

        assertEquals(ProposalStatus.MANUALLY_ACCEPTED, proposal.getStatus());
        assertEquals(result, proposal.getActiveDecision());

        verify(attemptRepo).save(any(ProposalDecisionAttempt.class));
        verify(proposalRepo).save(proposal);
    }

    @Test
    void createManualDecision_ShouldCreateAttemptAndUpdateProposal_WhenRejected() {
        // Arrange
        Long proposalId = 1L;
        Long moderatorId = 100L;
        String notes = "Bad angle";
        DecisionOutcome outcome = DecisionOutcome.REJECT;

        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);
        proposal.setStatus(ProposalStatus.SUBMITTED);

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));

        // Act
        ProposalDecisionAttempt result = manualDecisionService.createManualDecision(proposalId, outcome, notes, moderatorId);

        // Assert
        assertEquals(DecisionOutcome.REJECT, result.getOutcome());
        assertEquals(ProposalStatus.MANUALLY_REJECTED, proposal.getStatus());

        verify(attemptRepo).save(any(ProposalDecisionAttempt.class));
        verify(proposalRepo).save(proposal);
    }

    @Test
    void createManualDecision_ShouldThrowException_WhenProposalNotFound() {
        // Arrange
        Long proposalId = 999L;
        when(proposalRepo.findById(proposalId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            manualDecisionService.createManualDecision(proposalId, DecisionOutcome.ACCEPT, "notes", 1L)
        );
    }
}
