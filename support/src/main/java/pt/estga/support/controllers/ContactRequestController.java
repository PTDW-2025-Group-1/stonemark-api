package pt.estga.support.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.estga.support.ContactStatus;
import pt.estga.support.dtos.ContactRequestDto;
import pt.estga.support.entities.ContactRequest;
import pt.estga.support.services.ContactRequestService;

@RestController
@RequestMapping("/api/v1/contact-requests")
@RequiredArgsConstructor
@Tag(name = "Contact Requests", description = "Endpoints for contact requests.")
public class ContactRequestController {

    private final ContactRequestService service;

    @GetMapping
    public ResponseEntity<Page<ContactRequest>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(service.findAll(pageable));
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
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ContactRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam ContactStatus status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
