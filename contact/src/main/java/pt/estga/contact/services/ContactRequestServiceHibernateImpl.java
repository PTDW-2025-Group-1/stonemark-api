package pt.estga.contact.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.dtos.contact.ContactRequestDto;
import pt.estga.stonemark.entities.ContactRequest;
import pt.estga.stonemark.enums.ContactStatus;
import pt.estga.stonemark.exceptions.ContactNotFoundException;
import pt.estga.stonemark.repositories.ContactRequestRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactRequestServiceHibernateImpl implements ContactRequestService {

    private final ContactRequestRepository repository;

    @Override
    public ContactRequest create(ContactRequestDto dto) {
        ContactRequest contact = new ContactRequest();

        contact.setName(dto.name());
        contact.setEmail(dto.email());
        contact.setSubject(dto.subject());
        contact.setMessage(dto.message());
        contact.setStatus(ContactStatus.PENDING);
        contact.setCreatedAt(Instant.now());

        return repository.save(contact);
    }

    @Override
    public List<ContactRequest> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<ContactRequest> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public ContactRequest updateStatus(Long id, ContactStatus status) {
        ContactRequest contact = repository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));

        contact.setStatus(status);
        return repository.save(contact);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ContactNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
