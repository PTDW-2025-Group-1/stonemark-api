package pt.estga.stonemark.controllers.occurrence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.entities.content.MarkOccurrence;
import pt.estga.stonemark.services.content.MarkOccurrenceService;

@RestController
@RequestMapping("/api/mark-occurrences")
@RequiredArgsConstructor
public class MarkOccurrenceController {

    private final MarkOccurrenceService markOccurrenceService;


    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public Page<MarkOccurrence> getMarkOccurrences(Pageable pageable) {
        return markOccurrenceService.findAll(pageable);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<MarkOccurrence> getMarkOccurrence(@PathVariable Long id) {
        return markOccurrenceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public MarkOccurrence createMarkOccurrence(@RequestBody MarkOccurrence markOccurrence) {
        return markOccurrenceService.create(markOccurrence);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public MarkOccurrence updateMarkOccurrence(@PathVariable Long id, @RequestBody MarkOccurrence markOccurrence) {
        markOccurrence.setId(id);
        return markOccurrenceService.update(markOccurrence);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteMarkOccurrence(@PathVariable Long id) {
        markOccurrenceService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}