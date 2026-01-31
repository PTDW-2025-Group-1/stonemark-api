package pt.estga.proposal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties("proposal.decision")
public class ProposalDecisionProperties {

    /**
     * Automatic Decision Thresholds.
     * Defines the score limits for automatically accepting or rejecting a proposal.
     */
    private Integer automaticAcceptanceThreshold = 150;
    private Integer automaticRejectionThreshold = 10;
    private Boolean requireManualReviewForNewMonuments = true;

    /**
     * Priority Scoring Configuration.
     * Parameters used to calculate the priority of a proposal, influencing the order of review.
     */
    private Integer reputationBoostPerApprovedProposal = 2;
    private Integer maxReputationBoost = 40;
    private Integer newMonumentProposalBoost = 5;

    /**
     * Credibility Scoring Configuration.
     * Parameters used to calculate the credibility score of a proposal based on user history,
     * data completeness, and content matching.
     */
    private Integer baseScoreAuthenticatedUser = 10;
    private Integer credibilityBoostPerApprovedProposal = 5;
    private Integer maxCredibilityBoostApprovedProposals = 50;
    private Integer completenessScoreLocation = 10;
    private Integer completenessScoreUserNotes = 5;
    private Integer completenessScoreMediaFile = 10;
    private Integer monumentNameExactMatchBoost = 15;
    private Integer monumentNameSimilarMatchBoost = 10;
    private Double monumentNameSimilarityThreshold = 0.7;
    private Integer monumentNameWordMatchBoostPerWord = 5;
    private Integer minWordLengthForMatch = 3;
    private Integer maxWordTypoDistance = 2;
    private Integer maxCredibilityScore = 100;

}
