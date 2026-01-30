package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.Monument;
import pt.estga.proposal.config.ProposalDecisionProperties;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.shared.utils.StringSimilarityUtils;

@Service
@RequiredArgsConstructor
public class ProposalScoringService {

    private final ProposalDecisionProperties properties;

    public Integer calculatePriority(MarkOccurrenceProposal proposal) {
        int priority = 0;

        // Boost for New Monument Proposals - Small boost for complexity
        if (proposal.getMonumentName() != null) {
            priority += properties.getNewMonumentProposalBoost();
        }

        return priority;
    }

    public Integer calculateCredibilityScore(MarkOccurrenceProposal proposal) {
        int score = 0;

        // Base score for authenticated users
        if (proposal.getSubmittedBy() != null) {
            score += properties.getBaseScoreAuthenticatedUser();
        }

        // Completeness of data
        if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
            score += properties.getCompletenessScoreLocation();
        }
        if (proposal.getUserNotes() != null && !proposal.getUserNotes().isEmpty()) {
            score += properties.getCompletenessScoreUserNotes();
        }
        if (proposal.getOriginalMediaFile() != null) {
            score += properties.getCompletenessScoreMediaFile();
        }

        // Boost if suggested monument name resembles the found/linked monument name
        Monument existingMonument = proposal.getExistingMonument();
        if (existingMonument != null && proposal.getMonumentName() != null) {
            String suggestedName = proposal.getMonumentName();
            String actualName = existingMonument.getName();
            
            if (StringSimilarityUtils.containsIgnoreCase(suggestedName, actualName)) {
                score += properties.getMonumentNameExactMatchBoost();
            } else if (StringSimilarityUtils.calculateLevenshteinSimilarity(suggestedName, actualName) > properties.getMonumentNameSimilarityThreshold()) {
                score += properties.getMonumentNameSimilarMatchBoost();
            } else {
                int matchCount = StringSimilarityUtils.countMatchingWords(suggestedName, actualName, properties.getMinWordLengthForMatch(), properties.getMaxWordTypoDistance());
                if (matchCount > 0) {
                    score += properties.getMonumentNameWordMatchBoostPerWord() * matchCount;
                }
            }
        }

        return Math.min(score, properties.getMaxCredibilityScore()); // Normalize to 0-100
    }
}
