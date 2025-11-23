package pt.estga.stonemark.services.contact;

import pt.estga.stonemark.dtos.contact.ContactRequestDto;
import pt.estga.stonemark.entities.ContactRequest;
import pt.estga.stonemark.enums.ContactStatus;

import java.util.List;
import java.util.Optional;

public interface ContactRequestService {

    List<ContactRequest> findAll();

    Optional<ContactRequest> findById(Long id);

    ContactRequest create(ContactRequestDto dto);

    ContactRequest updateStatus(Long id, ContactStatus status);

    void delete(Long id);
}

