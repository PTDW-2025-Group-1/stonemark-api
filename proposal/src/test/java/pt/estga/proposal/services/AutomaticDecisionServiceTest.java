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
class AutomaticDecisionServiceTest {

    @Mock
    private ProposalDecisionAttemptRepository attemptRepo;

    @Mock
    private MarkOccurrenceProposalRepository proposalRepo;

    @InjectMocks
    private AutomaticDecisionService automaticDecisionService;

    @Test
    void run_ShouldAutoAccept_WhenPriorityIsHigh() {
        // Arrange
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setPriority(200); // > 150

        // Act
        ProposalDecisionAttempt result = automaticDecisionService.run(proposal);

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
    void run_ShouldAutoReject_WhenPriorityIsLow() {
        // Arrange
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setPriority(5); // < 10

        // Act
        ProposalDecisionAttempt result = automaticDecisionService.run(proposal);

        // Assert
        assertEquals(DecisionOutcome.REJECT, result.getOutcome());
        assertFalse(result.getConfident());
        assertEquals(ProposalStatus.AUTO_REJECTED, proposal.getStatus());

        verify(attemptRepo).save(any(ProposalDecisionAttempt.class));
        verify(proposalRepo).save(proposal);
    }

    @Test
    void run_ShouldBeInconclusive_WhenPriorityIsMedium() {
        // Arrange
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setPriority(100); // Between 10 and 150

        // Act
        ProposalDecisionAttempt result = automaticDecisionService.run(proposal);

        // Assert
        assertEquals(DecisionOutcome.INCONCLUSIVE, result.getOutcome());
        assertFalse(result.getConfident());
        assertEquals(ProposalStatus.UNDER_REVIEW, proposal.getStatus());

        verify(attemptRepo).save(any(ProposalDecisionAttempt.class));
        verify(proposalRepo).save(proposal);
    }

    @Test
    void run_ShouldBeInconclusive_WhenPriorityIsNull() {
        // Arrange
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setPriority(null);

        // Act
        ProposalDecisionAttempt result = automaticDecisionService.run(proposal);

        // Assert
        assertEquals(DecisionOutcome.INCONCLUSIVE, result.getOutcome());
        assertFalse(result.getConfident());
        assertEquals(ProposalStatus.UNDER_REVIEW, proposal.getStatus());

        verify(attemptRepo).save(any(ProposalDecisionAttempt.class));
        verify(proposalRepo).save(proposal);
    }

    @Test
    void rerunAutomaticDecision_ShouldFetchAndRun() {
        // Arrange
        Long proposalId = 1L;
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);
        proposal.setPriority(200);

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));

        // Act
        ProposalDecisionAttempt result = automaticDecisionService.rerunAutomaticDecision(proposalId);

        // Assert
        assertNotNull(result);
        assertEquals(DecisionOutcome.ACCEPT, result.getOutcome());
        verify(proposalRepo).findById(proposalId);
    }

    @Test
    void rerunAutomaticDecision_ShouldThrowException_WhenProposalNotFound() {
        // Arrange
        Long proposalId = 999L;
        when(proposalRepo.findById(proposalId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            automaticDecisionService.rerunAutomaticDecision(proposalId)
        );
    }
}
