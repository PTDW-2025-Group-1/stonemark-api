package pt.estga.stonemark.services.security.verification.email;

import org.springframework.stereotype.Component;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class EmailContentProviderFactory {

    private final Map<VerificationTokenPurpose, EmailContentProvider> providers = new EnumMap<>(VerificationTokenPurpose.class);

    public EmailContentProviderFactory(List<EmailContentProvider> providers) {
        for (EmailContentProvider provider : providers) {
            this.providers.put(provider.getPurpose(), provider);
        }
    }

    public EmailContentProvider getProvider(VerificationTokenPurpose purpose) {
                EmailContentProvider provider = providers.get(purpose);
        if (provider == null) {
            throw new IllegalArgumentException("No EmailContentProvider found for purpose: " + purpose);
        }
        return provider;
    }
}
