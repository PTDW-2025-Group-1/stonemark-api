package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.estga.content.dtos.MarkDto;
import pt.estga.content.entities.Mark;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.services.MarkService;
import pt.estga.file.services.MediaService;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/moderation/marks")
@RequiredArgsConstructor
@Tag(name = "Marks Moderation", description = "Moderation endpoints for marks.")
public class MarkModerationController {

    private final MarkService service;
    private final MarkMapper mapper;
    private final MediaService mediaService;

    @GetMapping("/management")
    public Page<MarkDto> getMarksManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAllManagement(pageable).map(mapper::toDto);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MarkDto> createMark(
            @RequestPart("data") MarkDto markDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Mark mark = mapper.toEntity(markDto);

        if (markDto.coverId() != null) {
            mediaService.findById(markDto.coverId()).ifPresent(mark::setCover);
        }

        Mark createdMark = service.create(mark, file);
        MarkDto response = mapper.toDto(createdMark);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MarkDto> updateMark(
            @PathVariable Long id,
            @RequestPart("data") MarkDto markDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Mark existingMark = service.findById(id)
                .orElseThrow(() -> new RuntimeException("Mark not found"));

        mapper.updateEntityFromDto(markDto, existingMark);

        if (markDto.coverId() != null) {
            mediaService.findById(markDto.coverId()).ifPresent(existingMark::setCover);
        }

        Mark updatedMark = service.update(existingMark, file);
        return ResponseEntity.ok(mapper.toDto(updatedMark));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMark(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MarkDto> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Mark updatedMark = service.updateCover(id, file);
        return ResponseEntity.ok(mapper.toDto(updatedMark));
    }
}
