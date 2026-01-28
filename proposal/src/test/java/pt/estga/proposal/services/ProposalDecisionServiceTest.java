package pt.estga.proposal.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.proposal.config.ProposalDecisionProperties;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;
import pt.estga.shared.exceptions.ResourceNotFoundException;
import pt.estga.user.entities.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProposalDecisionServiceTest {

    @Mock
    private ProposalDecisionAttemptRepository attemptRepo;

    @Mock
    private MarkOccurrenceProposalRepository proposalRepo;

    @Mock
    private ProposalDecisionProperties properties;

    @InjectMocks
    private ProposalDecisionService proposalDecisionService;

    // ==== Automatic Decision Tests ====

    @Test
    void makeAutomaticDecision_ShouldAutoAccept_WhenPriorityIsHigh() {
        // Arrange
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setPriority(200);
        when(properties.getAutomaticAcceptanceThreshold()).thenReturn(150);

        // Act
        ProposalDecisionAttempt result = proposalDecisionService.makeAutomaticDecision(proposal);

        // Assert
        assertEquals(DecisionType.AUTOMATIC, result.getType());
        assertEquals(DecisionOutcome.ACCEPT, result.getOutcome());
        assertTrue(result.getConfident());
        assertEquals(ProposalStatus.AUTO_ACCEPTED, proposal.getStatus());
        assertEquals(result, proposal.getActiveDecision());

        verify(attemptRepo).save(any(ProposalDecisionAttempt.class));
        verify(proposalRepo).save(proposal);
    }

    @Test
    void makeAutomaticDecision_ShouldAutoReject_WhenPriorityIsLow() {
        // Arrange
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setPriority(5);
        when(properties.getAutomaticAcceptanceThreshold()).thenReturn(150);
        when(properties.getAutomaticRejectionThreshold()).thenReturn(10);

        // Act
        ProposalDecisionAttempt result = proposalDecisionService.makeAutomaticDecision(proposal);

        // Assert
        assertEquals(DecisionOutcome.REJECT, result.getOutcome());
        assertFalse(result.getConfident());
        assertEquals(ProposalStatus.AUTO_REJECTED, proposal.getStatus());

        verify(attemptRepo).save(any(ProposalDecisionAttempt.class));
        verify(proposalRepo).save(proposal);
    }

    @Test
    void makeAutomaticDecision_ShouldBeInconclusive_WhenPriorityIsMedium() {
        // Arrange
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setPriority(100);
        when(properties.getAutomaticAcceptanceThreshold()).thenReturn(150);
        when(properties.getAutomaticRejectionThreshold()).thenReturn(10);

        // Act
        ProposalDecisionAttempt result = proposalDecisionService.makeAutomaticDecision(proposal);

        // Assert
        assertEquals(DecisionOutcome.INCONCLUSIVE, result.getOutcome());
        assertFalse(result.getConfident());
        assertEquals(ProposalStatus.UNDER_REVIEW, proposal.getStatus());

        verify(attemptRepo).save(any(ProposalDecisionAttempt.class));
        verify(proposalRepo).save(proposal);
    }

    @Test
    void makeAutomaticDecision_ById_ShouldFetchAndRun() {
        // Arrange
        Long proposalId = 1L;
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);
        proposal.setPriority(200);

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(properties.getAutomaticAcceptanceThreshold()).thenReturn(150);

        // Act
        ProposalDecisionAttempt result = proposalDecisionService.makeAutomaticDecision(proposalId);

        // Assert
        assertNotNull(result);
        assertEquals(DecisionOutcome.ACCEPT, result.getOutcome());
        verify(proposalRepo).findById(proposalId);
    }

    // ==== Manual Decision Tests ====

    @Test
    void makeManualDecision_ShouldCreateAttemptAndUpdateProposal_WhenAccepted() {
        // Arrange
        Long proposalId = 1L;
        Long moderatorId = 100L;
        String notes = "Looks good";
        DecisionOutcome outcome = DecisionOutcome.ACCEPT;

        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);
        proposal.setStatus(ProposalStatus.SUBMITTED);

        User moderator = new User();
        moderator.setId(moderatorId);

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));

        // Act
        ProposalDecisionAttempt result = proposalDecisionService.makeManualDecision(proposalId, outcome, notes, moderator);

        // Assert
        assertNotNull(result);
        assertEquals(DecisionType.MANUAL, result.getType());
        assertEquals(outcome, result.getOutcome());
        assertEquals(notes, result.getNotes());
        assertEquals(moderator, result.getDecidedBy());
        assertTrue(result.getConfident());

        assertEquals(ProposalStatus.MANUALLY_ACCEPTED, proposal.getStatus());
        assertEquals(result, proposal.getActiveDecision());

        verify(attemptRepo).save(any(ProposalDecisionAttempt.class));
        verify(proposalRepo).save(proposal);
    }

    @Test
    void makeManualDecision_ShouldThrowException_WhenProposalNotFound() {
        // Arrange
        Long proposalId = 999L;
        User moderator = new User();
        moderator.setId(1L);
        when(proposalRepo.findById(proposalId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            proposalDecisionService.makeManualDecision(proposalId, DecisionOutcome.ACCEPT, "notes", moderator)
        );
    }

    // ==== Activation Tests ====

    @Test
    void activateDecision_ShouldActivateManualAccept() {
        // Arrange
        Long proposalId = 1L;
        Long attemptId = 10L;

        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);

        ProposalDecisionAttempt attempt = new ProposalDecisionAttempt();
        attempt.setId(attemptId);
        attempt.setProposal(proposal);
        attempt.setType(DecisionType.MANUAL);
        attempt.setOutcome(DecisionOutcome.ACCEPT);

        when(attemptRepo.findById(attemptId)).thenReturn(Optional.of(attempt));

        // Act
        proposalDecisionService.activateDecision(attemptId);

        // Assert
        assertEquals(attempt, proposal.getActiveDecision());
        assertEquals(ProposalStatus.MANUALLY_ACCEPTED, proposal.getStatus());
        verify(proposalRepo).save(proposal);
    }

    @Test
    void deactivateDecision_ShouldDeactivateDecision() {
        // Arrange
        Long proposalId = 1L;
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);
        proposal.setStatus(ProposalStatus.MANUALLY_ACCEPTED);
        proposal.setActiveDecision(new ProposalDecisionAttempt());

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));

        // Act
        proposalDecisionService.deactivateDecision(proposalId);

        // Assert
        assertNull(proposal.getActiveDecision());
        assertEquals(ProposalStatus.UNDER_REVIEW, proposal.getStatus());
        verify(proposalRepo).save(proposal);
    }
}
