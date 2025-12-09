package pt.estga.auth.services.verification.email;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationPurpose;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class EmailContentProviderFactory {

    private final Map<VerificationPurpose, EmailContentProvider> providers = new EnumMap<>(VerificationPurpose.class);

    public EmailContentProviderFactory(List<EmailContentProvider> providers) {
        for (EmailContentProvider provider : providers) {
            this.providers.put(provider.getPurpose(), provider);
        }
    }

    public EmailContentProvider getProvider(VerificationPurpose purpose) {
                EmailContentProvider provider = providers.get(purpose);
        if (provider == null) {
            throw new IllegalArgumentException("No EmailContentProvider found for purpose: " + purpose);
        }
        return provider;
    }
}
