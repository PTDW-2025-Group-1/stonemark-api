package pt.estga.chatbots.core.infrastructure;

import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.features.auth.commands.StartAuthenticationCommand;
import pt.estga.chatbots.core.features.auth.commands.SubmitContactCommand;
import pt.estga.chatbots.core.features.common.commands.HelpCommand;
import pt.estga.chatbots.core.features.common.commands.StartCommand;
import pt.estga.chatbots.core.features.proposal.commands.*;
import pt.estga.chatbots.core.models.BotInput;

@Component
public class CommandFactory {
    public Command createCommand(BotInput input, ConversationContext context) {
        String currentState = context.getCurrentStateName();

        // Handle photo submission if the conversation is new or explicitly waiting for a photo.
        if (input.getType() == BotInput.InputType.PHOTO) {
            if (currentState == null || "START".equals(currentState) || "WAITING_FOR_PHOTO".equals(currentState)) {
                return new SubmitPhotoCommand(input);
            }
        }

        if (input.getText() != null) {
            if (input.getText().startsWith("/start")) {
                return new StartCommand(input);
            } else if (input.getText().startsWith("/help")) {
                return new HelpCommand(input);
            }
        }

        if (input.getCallbackData() != null) {
            String[] callbackDataParts = input.getCallbackData().split(":");
            String action = callbackDataParts[0];

            switch (action) {
                case "start_submission":
                    return new StartSubmissionCommand(input);
                case "start_verification":
                    return new StartAuthenticationCommand(input);
                case "confirm_mark_match":
                    boolean matches = "yes".equalsIgnoreCase(callbackDataParts[1]);
                    return new ConfirmMarkMatchCommand(input, matches);
                case "use_detected_coordinates":
                    return new UseDetectedCoordinatesCommand(input);
                case "send_location_manually":
                    return new SendLocationManuallyCommand(input);
                case "confirm_monument":
                    boolean confirmed = "yes".equalsIgnoreCase(callbackDataParts[1]);
                    Long monumentId = confirmed ? Long.valueOf(callbackDataParts[2]) : null;
                    return new ConfirmMonumentCommand(input, confirmed, monumentId);
                case "select_monument":
                    return new SelectMonumentCommand(input, Long.valueOf(callbackDataParts[1]));
                case "select_mark":
                    return new SelectMarkCommand(input, Long.valueOf(callbackDataParts[1]));
                case "propose_new_monument":
                    return new ProposeNewMonumentCommand(input);
                case "propose_new_mark":
                    return new ProposeNewMarkCommand(input);
            }
        }

        if (currentState != null) {
            switch (currentState) {
                case "AWAITING_CONTACT":
                    if (input.getType() == BotInput.InputType.CONTACT) {
                        return new SubmitContactCommand(input);
                    }
                    break;
                case "AWAITING_LOCATION":
                    if (input.getType() == BotInput.InputType.LOCATION) {
                        return new SubmitLocationCommand(input);
                    }
                    break;
                case "AWAITING_NEW_MONUMENT_NAME":
                    if (input.getType() == BotInput.InputType.TEXT) {
                        return new SubmitNewMonumentNameCommand(input);
                    }
                    break;
                case "AWAITING_NEW_MARK_DETAILS":
                case "AWAITING_NEW_MARK_NAME":
                    if (input.getType() == BotInput.InputType.TEXT) {
                        return new SubmitNewMarkDetailsCommand(input);
                    }
                    break;
            }
        }

        // Fallback to help command for any unhandled input
        return new HelpCommand(input);
    }
}
