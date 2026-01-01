package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.estga.content.dtos.MarkListDto;
import pt.estga.content.dtos.MarkUpdateDto;
import pt.estga.content.dtos.MarkDto;
import pt.estga.content.entities.Mark;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.services.MarkService;

@RestController
@RequestMapping("/api/v1/marks")
@RequiredArgsConstructor
@Tag(name = "Marks", description = "Endpoints for marks.")
public class MarkController {

    private final MarkService service;
    private final MarkMapper mapper;

    @GetMapping
    public Page<MarkListDto> getMarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAll(pageable).map(mapper::toListDto);
    }

    @GetMapping("/details")
    public Page<MarkDto> getDetailedMarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarkDto> getMark(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<MarkDto> updateMark(@PathVariable Long id, @RequestBody MarkUpdateDto markDto) {
        return service.findById(id)
                .map(existingMark -> {
                    mapper.updateEntityFromDto(markDto, existingMark);
                    Mark updatedMark = service.update(existingMark);
                    return ResponseEntity.ok(mapper.toDto(updatedMark));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteMark(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
