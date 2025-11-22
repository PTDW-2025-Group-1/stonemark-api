package pt.estga.stonemark.controllers.content;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.dtos.content.MarkDto;
import pt.estga.stonemark.entities.content.Mark;
import pt.estga.stonemark.mappers.MarkMapper;
import pt.estga.stonemark.services.content.MarkService;

@RestController
@RequestMapping("/api/v1/marks")
@RequiredArgsConstructor
public class MarkController {

    private final MarkService service;
    private final MarkMapper mapper;

    @GetMapping
    public Page<MarkDto> getMarks(Pageable pageable) {
        return service.findAll(pageable).map(mapper::markToMarkDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarkDto> getMark(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::markToMarkDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public MarkDto createMark(@RequestBody MarkDto markDto) {
        Mark mark = mapper.markDtoToMark(markDto);
        return mapper.markToMarkDto(service.create(mark));
    }

    @PutMapping("/{id}")
    public MarkDto updateMark(@PathVariable Long id, @RequestBody MarkDto markDto) {
        Mark mark = mapper.markDtoToMark(markDto);
        mark.setId(id);
        return mapper.markToMarkDto(service.update(mark));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMark(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
