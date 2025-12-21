package pt.estga.chatbots.telegram.state;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Contact;
import pt.estga.chatbots.telegram.context.ConversationContext;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.shared.models.Location;

public interface ConversationState {

    ProposalStatus getAssociatedStatus();

    default BotApiMethod<?> onEnter(ConversationContext context) {
        return null;
    }

    default BotApiMethod<?> handleTextMessage(ConversationContext context, String messageText) {
        return null;
    }

    default BotApiMethod<?> handlePhotoSubmission(ConversationContext context, byte[] photoData, String fileName) {
        return null;
    }

    default BotApiMethod<?> handleLocationSubmission(ConversationContext context, Location location) {
        return null;
    }

    default BotApiMethod<?> handleSubmitCommand(ConversationContext context) {
        return null;
    }

    default BotApiMethod<?> handleSkipCommand(ConversationContext context) {
        return null;
    }

    default BotApiMethod<?> handleContact(ConversationContext context, Contact contact) {
        return null;
    }
}
