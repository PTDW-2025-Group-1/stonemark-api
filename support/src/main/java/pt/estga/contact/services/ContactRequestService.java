package pt.estga.contact.services;

import pt.estga.contact.ContactStatus;
import pt.estga.contact.dtos.ContactRequestDto;
import pt.estga.contact.entities.ContactRequest;

import java.util.List;
import java.util.Optional;

public interface ContactRequestService {

    List<ContactRequest> findAll();

    Optional<ContactRequest> findById(Long id);

    ContactRequest create(ContactRequestDto dto);

    ContactRequest updateStatus(Long id, ContactStatus status);

    void delete(Long id);
}

