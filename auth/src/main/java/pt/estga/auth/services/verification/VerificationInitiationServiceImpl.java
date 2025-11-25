package pt.estga.auth.services.verification;

import org.springframework.stereotype.Service;
import pt.estga.auth.services.verification.commands.VerificationCommand;

@Service
public class VerificationInitiationServiceImpl implements VerificationInitiationService {

    @Override
    public void initiate(VerificationCommand command) {
        command.execute();
    }
}
