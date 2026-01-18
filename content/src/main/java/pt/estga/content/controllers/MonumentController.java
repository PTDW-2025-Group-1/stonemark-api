package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import pt.estga.content.dtos.MonumentListDto;
import pt.estga.content.dtos.MonumentMapDto;
import pt.estga.content.dtos.MonumentRequestDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.content.entities.Monument;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.content.services.MonumentService;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.services.MediaService;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/monuments")
@RequiredArgsConstructor
@Tag(name = "Monuments", description = "Endpoints for monuments.")
public class MonumentController {

    private final MonumentService service;
    private final MonumentMapper mapper;
    private final MediaService mediaService;

    @GetMapping
    public Page<MonumentListDto> getMonuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAll(pageable).map(mapper::toListDto);
    }

    @GetMapping("/details")
    public Page<MonumentResponseDto> getDetailedMonuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAllWithDivisions(pageable).map(mapper::toResponseDto);
    }

    @GetMapping("/map")
    public Page<MonumentMapDto> getAllForMap(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toMapDto);
    }

    @GetMapping("/search")
    public Page<MonumentListDto> searchMonuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return service.searchByName(query, pageable).map(mapper::toListDto);
    }

    @PostMapping("/search/polygon")
    public Page<MonumentListDto> searchMonumentsByPolygon(
            @RequestBody String geoJson,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return service.findByPolygon(geoJson, pageable).map(mapper::toListDto);
    }

    @GetMapping("/division/{id}")
    public Page<MonumentListDto> getMonumentsByDivision(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return service.findByDivisionId(id, pageable).map(mapper::toListDto);
    }

    @GetMapping("/popular")
    public List<MonumentListDto> getPopularMonuments(
            @RequestParam(defaultValue = "6") int limit
    ) {
        int safeLimit = Math.min(limit, 50);
        return mapper.toListDto(service.findPopular(safeLimit));
    }

    @GetMapping("/latest")
    public List<MonumentListDto> getLatestMonuments(
            @RequestParam(defaultValue = "6") int limit
    ) {
        int safeLimit = Math.min(limit, 50);
        return mapper.toListDto(service.findLatest(safeLimit));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countMonuments() {
        return ResponseEntity.ok(service.count());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonumentResponseDto> getMonumentById(
            @PathVariable Long id
    ) {
        return service.findById(id)
                .map(mapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MODERATOR')")
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
    @PreAuthorize("hasRole('MODERATOR')")
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
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteMonument(
            @PathVariable Long id
    ) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MODERATOR')")
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
