package pt.estga.stonemark.controllers.content;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.dtos.content.MarkOccurrenceDto;
import pt.estga.stonemark.entities.content.MarkOccurrence;
import pt.estga.stonemark.mappers.MarkOccurrenceMapper;
import pt.estga.stonemark.services.content.MarkOccurrenceService;

@RestController
@RequestMapping("/api/v1/mark-occurrences")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrences", description = "Endpoints for mark occurrences.")
public class MarkOccurrenceController {

    private final MarkOccurrenceService service;
    private final MarkOccurrenceMapper mapper;

    @GetMapping
    public Page<MarkOccurrenceDto> getMarkOccurrences(Pageable pageable) {
        return service.findAll(pageable)
                .map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarkOccurrenceDto> getMarkOccurrence(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public MarkOccurrenceDto createMarkOccurrence(@RequestBody MarkOccurrenceDto markOccurrenceDto) {
        MarkOccurrence markOccurrence = mapper.toEntity(markOccurrenceDto);
        return mapper.toDto(service.create(markOccurrence));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public MarkOccurrenceDto updateMarkOccurrence(@PathVariable Long id, @RequestBody MarkOccurrenceDto markOccurrenceDto) {
        MarkOccurrence markOccurrence = mapper.toEntity(markOccurrenceDto);
        markOccurrence.setId(id);
        return mapper.toDto(service.update(markOccurrence));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteMarkOccurrence(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
