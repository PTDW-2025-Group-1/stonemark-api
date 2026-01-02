package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.Monument;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.shared.utils.StringSimilarityUtils;

@Service
@RequiredArgsConstructor
public class ProposalScoringService {

    private final MarkOccurrenceProposalService proposalService;

    public Integer calculatePriority(MarkOccurrenceProposal proposal) {
        int priority = 0;

        // User Reputation Boost (based on approved proposals)
        Long userId = proposal.getSubmittedById();
        if (userId != null) {
            long approvedCount = proposalService.countApprovedProposalsByUserId(userId);
            
            // Cap the reputation boost at +40 (e.g., 2 points per approved proposal up to 40)
            int reputationBoost = (int) Math.min(approvedCount * 2, 40);
            priority += reputationBoost;
        }

        // Boost for New Monument Proposals (+5) - Small boost for complexity
        if (proposal.getMonumentName() != null) {
            priority += 5;
        }

        return priority;
    }

    public Integer calculateCredibilityScore(MarkOccurrenceProposal proposal) {
        int score = 0;

        // Base score for authenticated users
        if (proposal.getSubmittedById() != null) {
            score += 10;
        }

        // Credibility based on past approved proposals
        Long userId = proposal.getSubmittedById();
        if (userId != null) {
            long approvedCount = proposalService.countApprovedProposalsByUserId(userId);
            score += (int) Math.min(approvedCount * 5, 50); // Cap at 50
        }

        // Completeness of data
        if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
            score += 10;
        }
        if (proposal.getUserNotes() != null && !proposal.getUserNotes().isEmpty()) {
            score += 5;
        }
        if (proposal.getOriginalMediaFile() != null) {
            score += 10;
        }

        // Boost if suggested monument name resembles the found/linked monument name
        Monument existingMonument = proposal.getExistingMonument();
        if (existingMonument != null && proposal.getMonumentName() != null) {
            String suggestedName = proposal.getMonumentName();
            String actualName = existingMonument.getName();
            
            if (StringSimilarityUtils.containsIgnoreCase(suggestedName, actualName)) {
                score += 15;
            } else if (StringSimilarityUtils.calculateLevenshteinSimilarity(suggestedName, actualName) > 0.7) {
                score += 10;
            } else {
                int matchCount = StringSimilarityUtils.countMatchingWords(suggestedName, actualName, 3, 2);
                if (matchCount > 0) {
                    score += 5 * matchCount;
                }
            }
        }

        return Math.min(score, 100); // Normalize to 0-100
    }
}
