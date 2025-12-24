package pt.estga.chatbots.core.proposal.service;

import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.List;

public interface MultipleMarkMatchProcessor {
    List<BotResponse> processMultipleMatches(
            ConversationContext context,
            MarkOccurrenceProposal proposal,
            List<String> markIds
    );
}
