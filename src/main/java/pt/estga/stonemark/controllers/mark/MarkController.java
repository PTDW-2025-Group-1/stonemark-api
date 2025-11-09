package pt.estga.stonemark.controllers.mark;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.entities.content.Mark;
import pt.estga.stonemark.services.content.MarkService;

@RestController
@RequestMapping("/api/marks")
@RequiredArgsConstructor
public class MarkController {
    private final MarkService markService;

    @GetMapping
    public Page<Mark> getMarks(Pageable pageable) {
        return markService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mark> getMark(@PathVariable Long id) {
        return markService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mark createMark(@RequestBody Mark mark) {
        return markService.create(mark);
    }

    @PutMapping("/{id}")
    public Mark updateMark(@PathVariable Long id, @RequestBody Mark mark) {
        mark.setId(id);
        return markService.update(mark);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMark(@PathVariable Long id) {
        markService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}