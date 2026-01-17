package pt.estga.support.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.support.ContactStatus;
import pt.estga.support.dtos.ContactRequestDto;
import pt.estga.support.entities.ContactRequest;

import java.util.List;
import java.util.Optional;

public interface ContactRequestService {

    Page<ContactRequest> findAll(Pageable pageable);

    Optional<ContactRequest> findById(Long id);

    ContactRequest create(ContactRequestDto dto);

    ContactRequest updateStatus(Long id, ContactStatus status);

    void delete(Long id);
}

