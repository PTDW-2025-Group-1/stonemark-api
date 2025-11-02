package pt.estga.stonemark.services.security.verification;

import org.springframework.stereotype.Service;
import pt.estga.stonemark.services.security.verification.commands.VerificationCommand;

@Service
public class VerificationInitiationServiceImpl implements VerificationInitiationService {

    @Override
    public void initiate(VerificationCommand command) {
        command.execute();
    }
}
