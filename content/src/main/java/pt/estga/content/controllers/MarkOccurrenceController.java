package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.estga.content.dtos.*;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.mappers.MarkOccurrenceMapper;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.content.services.MarkOccurrenceService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;

import java.io.IOException;
import java.net.URI;
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
    private final MediaService mediaService;

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
    public Page<MarkOccurrenceListDto> getOccurrencesByMark(
            @PathVariable Long markId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        return service.findByMarkId(markId, pageable)
                .map(mapper::toListDto);
    }

    @GetMapping("/map/by-mark/{markId}")
    public List<MarkOccurrenceMapDto> getOccurrencesForMapByMark(@PathVariable Long markId) {
        return service.findByMarkIdForMap(markId)
                .stream()
                .map(mapper::toMapDto)
                .toList();
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
        return markMapper.toDto(service.findAvailableMarksByMonumentId(monumentId));
    }

    @GetMapping("/filters/monuments-by-mark")
    public List<MonumentListDto> getAvailableMonumentsByMark(@RequestParam Long markId) {
        return monumentMapper.toListDto(service.findAvailableMonumentsByMarkId(markId));
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<MarkOccurrenceDto> createMarkOccurrence(
            @RequestPart("data") MarkOccurrenceDto markOccurrenceDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        MarkOccurrence markOccurrence = mapper.toEntity(markOccurrenceDto);

        if (file != null && !file.isEmpty()) {
            MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
            markOccurrence.setCover(mediaFile);
        } else if (markOccurrenceDto.coverId() != null) {
            mediaService.findById(markOccurrenceDto.coverId()).ifPresent(markOccurrence::setCover);
        }

        MarkOccurrence createdMarkOccurrence = service.create(markOccurrence);
        MarkOccurrenceDto response = mapper.toDto(createdMarkOccurrence);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<MarkOccurrenceDto> updateMarkOccurrence(
            @PathVariable Long id,
            @RequestPart("data") MarkOccurrenceDto markOccurrenceDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        MarkOccurrence existingMarkOccurrence = service.findById(id)
                .orElseThrow(() -> new RuntimeException("MarkOccurrence not found"));

        mapper.updateEntityFromDto(markOccurrenceDto, existingMarkOccurrence);

        if (file != null && !file.isEmpty()) {
            MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
            existingMarkOccurrence.setCover(mediaFile);
        } else if (markOccurrenceDto.coverId() != null) {
            mediaService.findById(markOccurrenceDto.coverId()).ifPresent(existingMarkOccurrence::setCover);
        }

        MarkOccurrence updatedMarkOccurrence = service.update(existingMarkOccurrence);
        return ResponseEntity.ok(mapper.toDto(updatedMarkOccurrence));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteMarkOccurrence(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<MarkOccurrenceDto> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        MarkOccurrence markOccurrence = service.findById(id)
                .orElseThrow(() -> new RuntimeException("MarkOccurrence not found"));

        MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
        markOccurrence.setCover(mediaFile);
        MarkOccurrence updatedMarkOccurrence = service.update(markOccurrence);

        return ResponseEntity.ok(mapper.toDto(updatedMarkOccurrence));
    }
}
