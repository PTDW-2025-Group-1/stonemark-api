package pt.estga.chatbots.context;

public enum VerificationState implements ConversationState {
    AWAITING_VERIFICATION_METHOD,
    AWAITING_CONTACT,
    AWAITING_VERIFICATION_CODE
}
