package pt.estga.stonemark.services.contact;

import pt.estga.stonemark.dtos.contact.ContactRequestDto;
import pt.estga.stonemark.entities.Contact;
import pt.estga.stonemark.enums.ContactStatus;

import java.util.List;
import java.util.Optional;

public interface ContactService {

    List<Contact> findAll();

    Optional<Contact> findById(Long id);

    Contact create(ContactRequestDto dto);

    Contact updateStatus(Long id, ContactStatus status);

    void delete(Long id);
}

