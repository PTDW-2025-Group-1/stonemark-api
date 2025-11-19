package pt.estga.stonemark.services.contact;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.dtos.contact.ContactRequestDto;
import pt.estga.stonemark.entities.Contact;
import pt.estga.stonemark.enums.ContactStatus;
import pt.estga.stonemark.exceptions.ContactNotFoundException;
import pt.estga.stonemark.repositories.ContactRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactServiceHibernateImpl implements ContactService {

    private final ContactRepository repository;

    @Override
    public Contact create(ContactRequestDto dto) {
        Contact contact = new Contact();

        contact.setName(dto.name());
        contact.setEmail(dto.email());
        contact.setSubject(dto.subject());
        contact.setMessage(dto.message());
        contact.setStatus(ContactStatus.PENDING);
        contact.setCreatedAt(Instant.now());

        return repository.save(contact);
    }

    @Override
    public List<Contact> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Contact> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Contact updateStatus(Long id, ContactStatus status) {
        Contact contact = repository.findById(id)
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
