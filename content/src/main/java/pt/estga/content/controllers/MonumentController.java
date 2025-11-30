package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.estga.content.dtos.MonumentDetailedResponseDto;
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
    public Page<MonumentResponseDto> getMonuments(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toResponseDto);
    }

    @GetMapping("/latest")
    public List<MonumentResponseDto> getLatestMonuments() {
        return service.findLatest(6)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonumentResponseDto> getMonument(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MonumentResponseDto> createMonument(@RequestBody MonumentRequestDto monumentDto) {
        Monument monument = mapper.toEntity(monumentDto);
        Monument createdMonument = service.create(monument);
        MonumentResponseDto createdMonumentDto = mapper.toResponseDto(createdMonument);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdMonumentDto.id())
                .toUri();
        return ResponseEntity.created(location).body(createdMonumentDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonumentDetailedResponseDto> updateMonument(@PathVariable Long id, @RequestBody MonumentRequestDto monumentDto) {
        Monument monument = mapper.toEntity(monumentDto);
        monument.setId(id);
        Monument updatedMonument = service.update(monument);
        return ResponseEntity.ok(mapper.toDetailedResponseDto(updatedMonument));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonument(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}