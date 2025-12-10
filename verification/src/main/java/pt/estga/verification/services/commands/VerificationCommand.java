package pt.estga.verification.services.commands;

public interface VerificationCommand<T> {

    void execute(T parameter);

}
