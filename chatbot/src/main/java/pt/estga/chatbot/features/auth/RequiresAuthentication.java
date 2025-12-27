package pt.estga.chatbot.features.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the authentication requirements for a ConversationStateHandler.
 * By default, all handlers require authentication.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresAuthentication {
    /**
     * @return true if authentication is required, false otherwise.
     */
    boolean value() default true;
}
