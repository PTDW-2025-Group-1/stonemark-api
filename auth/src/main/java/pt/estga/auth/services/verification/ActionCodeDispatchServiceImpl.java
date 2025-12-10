package pt.estga.auth.services.verification;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.auth.entities.ActionCode;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionCodeDispatchServiceImpl implements ActionCodeDispatchService {

    private final List<pt.estga.auth.services.verification.ContactVerificationService> contactVerificationServices;
    private Map<ContactType, ContactVerificationService> serviceMap;

    @PostConstruct
    public void init() {
        serviceMap = contactVerificationServices.stream()
                .collect(Collectors.toMap(ContactVerificationService::getContactType, Function.identity()));
    }

    @Override
    public void sendVerification(UserContact userContact, ActionCode code) {
        ContactVerificationService service = serviceMap.get(userContact.getType());
        if (service == null) {
            throw new IllegalArgumentException("Unsupported contact type for verification: " + userContact.getType());
        }
        service.sendVerification(userContact.getValue(), code);
    }
}
