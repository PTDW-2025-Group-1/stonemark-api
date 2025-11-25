package pt.estga.contact;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.shared.dtos.contact.ContactRequestDto;
import pt.estga.stonemark.entities.ContactRequest;
import pt.estga.stonemark.enums.ContactStatus;
import pt.estga.stonemark.services.contact.ContactRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contact-requests")
@RequiredArgsConstructor
@Tag(name = "Contact Requests", description = "Endpoints for contact requests.")
public class ContactRequestController {

    private final ContactRequestService service;

    @GetMapping
    public ResponseEntity<List<ContactRequest>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactRequest> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ContactRequest> create(@Valid @RequestBody ContactRequestDto dto) {
        ContactRequest created = service.create(dto);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ContactRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam ContactStatus status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
