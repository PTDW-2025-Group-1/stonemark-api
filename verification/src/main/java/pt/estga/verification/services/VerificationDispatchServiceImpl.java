package pt.estga.verification.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.verification.entities.ActionCode;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VerificationDispatchServiceImpl implements VerificationDispatchService {

    private final List<ContactVerificationService> contactVerificationServices;
    private Map<ContactType, ContactVerificationService> serviceMap;

    @PostConstruct
    public void init() {
        serviceMap = contactVerificationServices.stream()
                .collect(Collectors.toMap(ContactVerificationService::getContactType, Function.identity()));
    }

    @Override
    public void sendVerification(UserContact userContact, ActionCode actionCode) {
        ContactVerificationService service = serviceMap.get(userContact.getType());
        if (service == null) {
            throw new IllegalArgumentException("Unsupported contact type for verification: " + userContact.getType());
        }
        service.sendVerification(userContact.getValue(), actionCode);
    }
}
