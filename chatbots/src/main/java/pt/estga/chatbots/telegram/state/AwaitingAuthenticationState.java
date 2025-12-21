package pt.estga.chatbots.telegram.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Contact;
import pt.estga.chatbots.telegram.context.ConversationContext;
import pt.estga.chatbots.telegram.message.TelegramBotMessageFactory;
import pt.estga.chatbots.telegram.services.TelegramAuthService;
import pt.estga.chatbots.telegram.state.factory.StateFactory;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.user.entities.User;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AwaitingAuthenticationState implements ConversationState {

    private final TelegramAuthService authService;
    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.AWAITING_AUTHENTICATION;
    }

    @Override
    public BotApiMethod<?> handleContact(ConversationContext context, Contact contact) {
        Optional<User> userOptional = authService.authenticateUser(String.valueOf(context.getChatId()), contact.getPhoneNumber());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            context.setUserId(user.getId());
            context.setState(stateFactory.createState(ProposalStatus.IN_PROGRESS));
            return messageFactory.createAuthenticationSuccessMessage(context.getChatId(), user.getFirstName());
        } else {
            return messageFactory.createAuthenticationFailedMessage(context.getChatId());
        }
    }
}
