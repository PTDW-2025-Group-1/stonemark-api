package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.estga.content.dtos.MonumentRequestDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.content.entities.Monument;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.content.services.MonumentService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin/monuments")
@RequiredArgsConstructor
@Tag(name = "Monuments Management", description = "Management endpoints for monuments.")
public class MonumentAdminController {

    private final MonumentService service;
    private final MonumentMapper mapper;
    private final MediaService mediaService;

    @GetMapping()
    public Page<MonumentResponseDto> getMonumentsManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAllWithDivisionsManagement(pageable).map(mapper::toResponseDto);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MonumentResponseDto> createMonument(
            @RequestPart("data") @Valid MonumentRequestDto monumentDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Monument monument = mapper.toEntity(monumentDto);

        if (file != null && !file.isEmpty()) {
            MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
            monument.setCover(mediaFile);
        } else if (monumentDto.coverId() != null) {
            mediaService.findById(monumentDto.coverId()).ifPresent(monument::setCover);
        }

        Monument createdMonument = service.create(monument);
        MonumentResponseDto response = mapper.toResponseDto(createdMonument);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MonumentResponseDto> updateMonument(
            @PathVariable Long id,
            @RequestPart("data") @Valid MonumentRequestDto monumentDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Monument existingMonument = service.findById(id)
                .orElseThrow(() -> new RuntimeException("Monument not found"));

        mapper.updateEntityFromDto(monumentDto, existingMonument);

        if (file != null && !file.isEmpty()) {
            MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
            existingMonument.setCover(mediaFile);
        } else if (monumentDto.coverId() != null) {
            mediaService.findById(monumentDto.coverId()).ifPresent(existingMonument::setCover);
        }

        Monument updatedMonument = service.update(existingMonument);
        return ResponseEntity.ok(mapper.toResponseDto(updatedMonument));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonument(
            @PathVariable Long id
    ) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MonumentResponseDto> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Monument monument = service.findById(id)
                .orElseThrow(() -> new RuntimeException("Monument not found"));

        MediaFile mediaFile = mediaService.save(file.getInputStream(), file.getOriginalFilename());
        monument.setCover(mediaFile);
        Monument updatedMonument = service.update(monument);

        return ResponseEntity.ok(mapper.toResponseDto(updatedMonument));
    }
}
