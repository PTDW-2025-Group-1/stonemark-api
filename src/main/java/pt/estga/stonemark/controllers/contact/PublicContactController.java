package pt.estga.stonemark.controllers.contact;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.dtos.contact.ContactRequestDto;
import pt.estga.stonemark.entities.Contact;
import pt.estga.stonemark.services.contact.ContactService;

@RestController
@RequestMapping("/api/v1/public/contact")
@RequiredArgsConstructor
public class PublicContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<Contact> createContact(@Valid @RequestBody ContactRequestDto dto) {
        Contact saved = contactService.create(dto);
        return ResponseEntity.ok(saved);
    }
}

