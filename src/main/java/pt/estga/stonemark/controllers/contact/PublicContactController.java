package pt.estga.stonemark.controllers.contact;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.dtos.contact.ContactRequestDto;
import pt.estga.stonemark.entities.ContactRequest;
import pt.estga.stonemark.services.contact.ContactRequestService;

@RestController
@RequestMapping("/api/v1/public/contact-requests")
@RequiredArgsConstructor
public class PublicContactController {

    private final ContactRequestService service;

    @PostMapping
    public ResponseEntity<ContactRequest> create(@Valid @RequestBody ContactRequestDto dto) {
        ContactRequest created = service.create(dto);
        return ResponseEntity.ok(created);
    }
}

