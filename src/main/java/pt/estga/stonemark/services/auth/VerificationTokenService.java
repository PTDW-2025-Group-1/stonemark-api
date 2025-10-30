package pt.estga.stonemark.services.auth;

import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.repositories.token.VerificationTokenRepository;

@Service
public class VerificationTokenService extends BaseTokenServiceImpl<VerificationToken, VerificationTokenRepository> {

    public VerificationTokenService(VerificationTokenRepository repository) {
        super(repository);
    }
}
