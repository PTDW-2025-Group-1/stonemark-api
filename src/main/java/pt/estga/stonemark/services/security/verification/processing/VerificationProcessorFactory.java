package pt.estga.stonemark.services.security.verification.processing;

import org.springframework.stereotype.Component;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class VerificationProcessorFactory {

    private final Map<VerificationTokenPurpose, VerificationProcessor> processors = new EnumMap<>(VerificationTokenPurpose.class);

    public VerificationProcessorFactory(List<VerificationProcessor> processors) {
        for (VerificationProcessor processor : processors) {
            this.processors.put(processor.getPurpose(), processor);
        }
    }

    public VerificationProcessor getProcessor(VerificationTokenPurpose purpose) {
        VerificationProcessor processor = processors.get(purpose);
        if (processor == null) {
            throw new IllegalArgumentException("No VerificationProcessor found for purpose: " + purpose);
        }
        return processor;
    }
}
