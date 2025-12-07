package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.estga.content.dtos.MarkOccurrenceDto;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.mappers.MarkOccurrenceMapper;
import pt.estga.content.services.MarkOccurrenceService;

import java.util.List;

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

    @GetMapping("/by-mark/{markId}")
    public ResponseEntity<?> getOccurrencesByMark(@PathVariable Long markId) {
        var occurrences = service.findByMarkId(markId)
                .stream()
                .map(mapper::toDto)
                .toList();

        return ResponseEntity.ok(occurrences);
    }

    @GetMapping("/latest")
    public List<MarkOccurrenceDto> getLatestMarkOccurrences(
            @RequestParam(defaultValue = "6") int limit
    ) {
        int safeLimit = Math.min(limit, 50);
        return service.findLatest(safeLimit)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @GetMapping("/count-by-monument/{monumentId}")
    public ResponseEntity<Long> countByMonument(@PathVariable Long monumentId) {
        return ResponseEntity.ok(service.countByMonumentId(monumentId));
    }

    @GetMapping("/by-monument/{monumentId}")
    public Page<MarkOccurrenceDto> getOccurrencesByMonument(
            @PathVariable Long monumentId,
            Pageable pageable
    ) {
        return service.findByMonumentId(monumentId, pageable)
                .map(mapper::toDto);
    }

    @GetMapping("/filter")
    public Page<MarkOccurrenceDto> filterByMonument(
            @RequestParam Long monumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findByMonumentId(monumentId, pageable)
                .map(mapper::toDto);
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
