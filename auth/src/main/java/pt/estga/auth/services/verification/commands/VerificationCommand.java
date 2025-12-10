package pt.estga.auth.services.verification.commands;

public interface VerificationCommand<T> {

    void execute(T parameter);

}
