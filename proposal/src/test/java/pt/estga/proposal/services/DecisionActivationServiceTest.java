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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionActivationServiceTest {

    @Mock
    private MarkOccurrenceProposalRepository proposalRepo;

    @Mock
    private ProposalDecisionAttemptRepository attemptRepo;

    @InjectMocks
    private DecisionActivationService decisionActivationService;

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

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(attemptRepo.findById(attemptId)).thenReturn(Optional.of(attempt));

        // Act
        decisionActivationService.activateDecision(proposalId, attemptId);

        // Assert
        assertEquals(attempt, proposal.getActiveDecision());
        assertEquals(ProposalStatus.MANUALLY_ACCEPTED, proposal.getStatus());
        verify(proposalRepo).save(proposal);
    }

    @Test
    void activateDecision_ShouldActivateAutomaticReject() {
        // Arrange
        Long proposalId = 1L;
        Long attemptId = 10L;

        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);

        ProposalDecisionAttempt attempt = new ProposalDecisionAttempt();
        attempt.setId(attemptId);
        attempt.setProposal(proposal);
        attempt.setType(DecisionType.AUTOMATIC);
        attempt.setOutcome(DecisionOutcome.REJECT);

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(attemptRepo.findById(attemptId)).thenReturn(Optional.of(attempt));

        // Act
        decisionActivationService.activateDecision(proposalId, attemptId);

        // Assert
        assertEquals(attempt, proposal.getActiveDecision());
        assertEquals(ProposalStatus.AUTO_REJECTED, proposal.getStatus());
        verify(proposalRepo).save(proposal);
    }

    @Test
    void activateDecision_ShouldThrowException_WhenAttemptDoesNotBelongToProposal() {
        // Arrange
        Long proposalId = 1L;
        Long attemptId = 10L;

        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);

        MarkOccurrenceProposal otherProposal = new MarkOccurrenceProposal();
        otherProposal.setId(2L);

        ProposalDecisionAttempt attempt = new ProposalDecisionAttempt();
        attempt.setId(attemptId);
        attempt.setProposal(otherProposal); // Different proposal

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(attemptRepo.findById(attemptId)).thenReturn(Optional.of(attempt));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            decisionActivationService.activateDecision(proposalId, attemptId)
        );
    }

    @Test
    void activateDecision_ShouldThrowException_WhenProposalNotFound() {
        // Arrange
        Long proposalId = 999L;
        Long attemptId = 10L;

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            decisionActivationService.activateDecision(proposalId, attemptId)
        );
    }

    @Test
    void activateDecision_ShouldThrowException_WhenAttemptNotFound() {
        // Arrange
        Long proposalId = 1L;
        Long attemptId = 999L;

        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setId(proposalId);

        when(proposalRepo.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(attemptRepo.findById(attemptId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            decisionActivationService.activateDecision(proposalId, attemptId)
        );
    }
}
