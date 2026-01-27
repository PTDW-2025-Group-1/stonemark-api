package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.estga.content.dtos.MarkOccurrenceDto;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.mappers.MarkOccurrenceMapper;
import pt.estga.content.services.MarkOccurrenceService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;
import pt.estga.shared.exceptions.ResourceNotFoundException;
import pt.estga.shared.models.AppPrincipal;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin/mark-occurrences")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrences Management", description = "Moderation endpoints for mark occurrences.")
public class MarkOccurrenceAdminController {

    private final MarkOccurrenceService service;
    private final MarkOccurrenceMapper mapper;
    private final MediaService mediaService;

    @GetMapping()
    public Page<MarkOccurrenceDto> getMarkOccurrencesManagement(Pageable pageable) {
        return service.findAllManagement(pageable)
                .map(mapper::toDto);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MarkOccurrenceDto> createMarkOccurrence(
            @RequestPart("data") MarkOccurrenceDto markOccurrenceDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal AppPrincipal principal
    ) throws IOException {
        MarkOccurrence markOccurrence = mapper.toEntity(markOccurrenceDto);
        MediaFile mediaFile = null;

        if (file != null && !file.isEmpty()) {
            mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
        } else if (markOccurrenceDto.coverId() != null) {
            mediaFile = mediaService.findById(markOccurrenceDto.coverId()).orElse(null);
        }

        if (principal != null) {
            markOccurrence.setAuthorId(principal.getId());
            markOccurrence.setAuthorName(principal.getUsername());
        }

        MarkOccurrence createdMarkOccurrence = service.create(markOccurrence, mediaFile);
        MarkOccurrenceDto response = mapper.toDto(createdMarkOccurrence);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MarkOccurrenceDto> updateMarkOccurrence(
            @PathVariable Long id,
            @RequestPart("data") MarkOccurrenceDto markOccurrenceDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        MarkOccurrence existingMarkOccurrence = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MarkOccurrence not found"));

        mapper.updateEntityFromDto(markOccurrenceDto, existingMarkOccurrence);
        MediaFile mediaFile = null;

        if (file != null && !file.isEmpty()) {
            mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
        } else if (markOccurrenceDto.coverId() != null) {
            mediaFile = mediaService.findById(markOccurrenceDto.coverId()).orElse(null);
        }

        MarkOccurrence updatedMarkOccurrence = service.update(existingMarkOccurrence, mediaFile);
        return ResponseEntity.ok(mapper.toDto(updatedMarkOccurrence));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarkOccurrence(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MarkOccurrenceDto> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        MarkOccurrence existingMarkOccurrence = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MarkOccurrence not found"));

        MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
        MarkOccurrence updatedMarkOccurrence = service.update(existingMarkOccurrence, mediaFile);

        return ResponseEntity.ok(mapper.toDto(updatedMarkOccurrence));
    }
}
