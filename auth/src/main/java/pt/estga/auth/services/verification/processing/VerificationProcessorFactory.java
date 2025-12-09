package pt.estga.auth.services.verification.processing;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationPurpose;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class VerificationProcessorFactory {

    private final Map<VerificationPurpose, VerificationProcessor> processors = new EnumMap<>(VerificationPurpose.class);

    public VerificationProcessorFactory(List<VerificationProcessor> processors) {
        for (VerificationProcessor processor : processors) {
            this.processors.put(processor.getPurpose(), processor);
        }
    }

    public VerificationProcessor getProcessor(VerificationPurpose purpose) {
                VerificationProcessor processor = processors.get(purpose);
        if (processor == null) {
            throw new IllegalArgumentException("No VerificationProcessor found for purpose: " + purpose);
        }
        return processor;
    }
}
