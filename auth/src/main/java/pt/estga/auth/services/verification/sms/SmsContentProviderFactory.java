package pt.estga.auth.services.verification.sms;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SmsContentProviderFactory {

    private final Map<VerificationTokenPurpose, SmsContentProvider> providers = new EnumMap<>(VerificationTokenPurpose.class);

    public SmsContentProviderFactory(List<SmsContentProvider> providers) {
        for (SmsContentProvider provider : providers) {
            this.providers.put(provider.getPurpose(), provider);
        }
    }

    public SmsContentProvider getProvider(VerificationTokenPurpose purpose) {
        SmsContentProvider provider = providers.get(purpose);
        if (provider == null) {
            throw new IllegalArgumentException("No SmsContentProvider found for purpose: " + purpose);
        }
        return provider;
    }
}
