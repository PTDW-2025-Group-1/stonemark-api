package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.estga.content.dtos.MarkDto;
import pt.estga.content.dtos.MarkOccurrenceDetailedDto;
import pt.estga.content.dtos.MarkOccurrenceDto;
import pt.estga.content.dtos.MarkOccurrenceListDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.mappers.MarkOccurrenceMapper;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.content.services.MarkOccurrenceService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mark-occurrences")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrences", description = "Endpoints for mark occurrences.")
public class MarkOccurrenceController {

    private final MarkOccurrenceService service;
    private final MarkOccurrenceMapper mapper;
    private final MarkMapper markMapper;
    private final MonumentMapper monumentMapper;

    @GetMapping
    public Page<MarkOccurrenceListDto> getMarkOccurrences(Pageable pageable) {
        return service.findAll(pageable)
                .map(mapper::toListDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarkOccurrenceDetailedDto> getMarkOccurrence(@PathVariable Long id) {
        return service.findByIdWithMonument(id)
                .map(mapper::toDetailedDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-mark/{markId}")
    public Page<MarkOccurrenceListDto> getOccurrencesByMark(
            @PathVariable Long markId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt")); // substitui "createdAt" pelo campo correto
        return service.findByMarkId(markId, pageable)
                .map(mapper::toListDto);
    }

    @GetMapping("/latest")
    public List<MarkOccurrenceListDto> getLatestMarkOccurrences(
            @RequestParam(defaultValue = "6") int limit
    ) {
        int safeLimit = Math.min(limit, 50);
        return service.findLatest(safeLimit)
                .stream()
                .map(mapper::toListDto)
                .toList();
    }

    @GetMapping("/count-by-monument/{monumentId}")
    public ResponseEntity<Long> countByMonument(@PathVariable Long monumentId) {
        return ResponseEntity.ok(service.countByMonumentId(monumentId));
    }

    @GetMapping("/count-by-mark/{markId}")
    public ResponseEntity<Long> countByMark(@PathVariable Long markId) {
        return ResponseEntity.ok(service.countByMarkId(markId));
    }

    @GetMapping("/count-monuments-by-mark/{markId}")
    public ResponseEntity<Long> countMonumentsByMark(@PathVariable Long markId) {
        long count = service.countDistinctMonumentsByMarkId(markId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/by-monument/{monumentId}")
    public Page<MarkOccurrenceListDto> getOccurrencesByMonument(
            @PathVariable Long monumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        return service.findByMonumentId(monumentId, pageable)
                .map(mapper::toListDto);
    }

    @GetMapping("/filters/marks-by-monument")
    public List<MarkDto> getAvailableMarksByMonument(@RequestParam Long monumentId) {
        return service.findAvailableMarksByMonumentId(monumentId)
                .stream()
                .map(markMapper::toDto)
                .toList();
    }

    @GetMapping("/filters/monuments-by-mark")
    public List<MonumentResponseDto> getAvailableMonumentsByMark(@RequestParam Long markId) {
        return service.findAvailableMonumentsByMarkId(markId)
                .stream()
                .map(monumentMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/filter-by-mark-and-monument")
    public Page<MarkOccurrenceListDto> filterByMarkAndMonument(
            @RequestParam Long markId,
            @RequestParam Long monumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        return service.findByMarkIdAndMonumentId(markId, monumentId, pageable)
                .map(mapper::toListDto);
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
