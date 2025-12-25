package pt.estga.chatbot.context;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker interface for all conversation states.
 * Implementations should be Enums.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ConversationState {
    String name();
}
