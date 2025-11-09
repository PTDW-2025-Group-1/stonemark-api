package pt.estga.stonemark.controllers.monument;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.entities.content.Monument;
import pt.estga.stonemark.services.content.MonumentService;

@RestController
@RequestMapping("/api/monuments")
@RequiredArgsConstructor

public class MonumentController {

    private final MonumentService monumentService;

    @GetMapping
    public Page<Monument> getMonuments(Pageable pageable) {
        return monumentService.findAll(pageable);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Monument> getMonument(@PathVariable Long id) {
        return monumentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    public Monument createMonument(@RequestBody Monument monument) {
        return monumentService.create(monument);
    }
    @PutMapping("/{id}")
    public Monument updateMonument(@PathVariable Long id, @RequestBody Monument monument) {
        monument.setId(id);
        return monumentService.update(monument);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonument(@PathVariable Long id) {
        monumentService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

