package pt.estga.auth.services.verification.telephone;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TelephoneContentProviderFactory {

    private final Map<VerificationTokenPurpose, TelephoneContentProvider> providers = new EnumMap<>(VerificationTokenPurpose.class);

    public TelephoneContentProviderFactory(List<TelephoneContentProvider> providers) {
        for (TelephoneContentProvider provider : providers) {
            this.providers.put(provider.getPurpose(), provider);
        }
    }

    public TelephoneContentProvider getProvider(VerificationTokenPurpose purpose) {
        TelephoneContentProvider provider = providers.get(purpose);
        if (provider == null) {
            throw new IllegalArgumentException("No TelephoneContentProvider found for purpose: " + purpose);
        }
        return provider;
    }
}
