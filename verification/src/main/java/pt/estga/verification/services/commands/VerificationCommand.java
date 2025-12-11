package pt.estga.verification.services.commands;

@FunctionalInterface
public interface VerificationCommand<T> {

    Runnable execute(T parameter);

}
