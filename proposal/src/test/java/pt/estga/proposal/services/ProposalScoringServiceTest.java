package pt.estga.proposal.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pt.estga.file.entities.MediaFile;
import pt.estga.proposal.config.ProposalDecisionProperties;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.user.entities.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProposalScoringServiceTest {

    @Mock
    private ProposalDecisionProperties properties;

    @InjectMocks
    private ProposalScoringService scoringService;

    @Test
    void calculateCredibilityScore_ShouldCalculateCorrectly_ForCompleteMarkOccurrenceProposal() {
        // Arrange
        when(properties.getBaseScoreAuthenticatedUser()).thenReturn(10);
        when(properties.getCompletenessScoreUserNotes()).thenReturn(5);
        when(properties.getCompletenessScoreLocation()).thenReturn(10);
        when(properties.getCompletenessScoreMediaFile()).thenReturn(10);
        when(properties.getMaxCredibilityScore()).thenReturn(100);

        User user = User.builder().id(1L).build();
        MediaFile mediaFile = MediaFile.builder().id(1L).build();
        
        MarkOccurrenceProposal proposal = MarkOccurrenceProposal.builder()
                .submittedBy(user)
                .userNotes("Some notes")
                .latitude(40.0)
                .longitude(-8.0)
                .originalMediaFile(mediaFile)
                .build();

        // Act
        Integer score = scoringService.calculateCredibilityScore(proposal);

        // Assert
        // 10 (User) + 5 (Notes) + 10 (Location) + 10 (Media) = 35
        assertEquals(35, score);
    }

    @Test
    void calculateCredibilityScore_ShouldCalculateCorrectly_ForMinimalMarkOccurrenceProposal() {
        // Arrange
        when(properties.getMaxCredibilityScore()).thenReturn(100);

        MarkOccurrenceProposal proposal = MarkOccurrenceProposal.builder()
                .build();

        // Act
        Integer score = scoringService.calculateCredibilityScore(proposal);

        // Assert
        // 0 (No User) + 0 (No Notes) + 0 (No Location) + 0 (No Media) = 0
        assertEquals(0, score);
    }

    @Test
    void calculateCredibilityScore_ShouldCapScoreAtMax() {
        // Arrange
        when(properties.getBaseScoreAuthenticatedUser()).thenReturn(50);
        when(properties.getCompletenessScoreUserNotes()).thenReturn(60);
        when(properties.getMaxCredibilityScore()).thenReturn(100);
        
        User user = User.builder().id(1L).build();
        MarkOccurrenceProposal proposal = MarkOccurrenceProposal.builder()
                .submittedBy(user)
                .userNotes("Notes")
                .build();

        // Act
        Integer score = scoringService.calculateCredibilityScore(proposal);

        // Assert
        // 50 + 60 = 110, but max is 100
        assertEquals(100, score);
    }

    @Test
    void calculatePriority_ShouldReturnZero_ForBasicMarkOccurrenceProposal() {
        // Arrange
        MarkOccurrenceProposal proposal = MarkOccurrenceProposal.builder().build();

        // Act
        Integer priority = scoringService.calculatePriority(proposal);

        // Assert
        assertEquals(0, priority);
    }
}
