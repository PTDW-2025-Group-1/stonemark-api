package pt.estga.stonemark.controllers.content;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.estga.stonemark.dtos.content.MonumentDto;
import pt.estga.stonemark.entities.content.Monument;
import pt.estga.stonemark.mappers.MonumentMapper;
import pt.estga.stonemark.services.content.MonumentService;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/monuments")
@RequiredArgsConstructor
@Tag(name = "Monuments", description = "Endpoints for monuments.")
public class MonumentController {

    private final MonumentService service;
    private final MonumentMapper mapper;

    @GetMapping
    public Page<MonumentDto> getMonuments(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonumentDto> getMonument(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MonumentDto> createMonument(@RequestBody MonumentDto monumentDto) {
        Monument monument = mapper.toEntity(monumentDto);
        Monument createdMonument = service.create(monument);
        MonumentDto createdMonumentDto = mapper.toDto(createdMonument);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdMonumentDto.id())
                .toUri();
        return ResponseEntity.created(location).body(createdMonumentDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonumentDto> updateMonument(@PathVariable Long id, @RequestBody MonumentDto monumentDto) {
        MonumentDto dtoWithId = new MonumentDto(id, monumentDto.name(), monumentDto.description(), monumentDto.latitude(), monumentDto.longitude());
        Monument monument = mapper.toEntity(dtoWithId);
        Monument updatedMonument = service.update(monument);
        return ResponseEntity.ok(mapper.toDto(updatedMonument));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonument(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}