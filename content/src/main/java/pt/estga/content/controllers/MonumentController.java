package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.estga.content.dtos.MonumentListDto;
import pt.estga.content.dtos.MonumentMapDto;
import pt.estga.content.dtos.MonumentRequestDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.content.entities.Monument;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.content.services.MonumentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/monuments")
@RequiredArgsConstructor
@Tag(name = "Monuments", description = "Endpoints for monuments.")
public class MonumentController {

    private final MonumentService service;
    private final MonumentMapper mapper;

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
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAll(pageable).map(mapper::toResponseDto);
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

    @GetMapping("/filter")
    public Page<MonumentListDto> filterMonumentsByCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return service.findByCity(city, pageable).map(mapper::toListDto);
    }

    @GetMapping("/latest")
    public List<MonumentListDto> getLatestMonuments(
            @RequestParam(defaultValue = "6") int limit
    ) {
        int safeLimit = Math.min(limit, 50);
        return service.findLatest(safeLimit)
                .stream()
                .map(mapper::toListDto)
                .toList();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countMonuments() {
        return ResponseEntity.ok(service.count());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonumentResponseDto> getMonument(
            @PathVariable Long id
    ) {
        return service.findById(id)
                .map(mapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MonumentResponseDto> createMonument(
            @Valid @RequestBody MonumentRequestDto monumentDto
    ) {
        Monument monument = mapper.toEntity(monumentDto);
        Monument createdMonument = service.create(monument);
        MonumentResponseDto response = mapper.toResponseDto(createdMonument);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonumentResponseDto> updateMonument(
            @PathVariable Long id,
            @Valid @RequestBody MonumentRequestDto monumentDto
    ) {
        Monument monument = mapper.toEntity(monumentDto);
        monument.setId(id);
        Monument updatedMonument = service.update(monument);
        return ResponseEntity.ok(mapper.toResponseDto(updatedMonument));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonument(
            @PathVariable Long id
    ) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
