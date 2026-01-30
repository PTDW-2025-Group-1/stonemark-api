package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.content.dtos.*;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.mappers.MarkOccurrenceMapper;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.content.services.MarkOccurrenceService;
import pt.estga.content.services.MarkSearchService;
import pt.estga.detection.model.DetectionResult;
import pt.estga.detection.service.DetectionService;
import pt.estga.shared.utils.VectorUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/mark-occurrences")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrences", description = "Endpoints for mark occurrences.")
public class MarkOccurrenceController {

    private final MarkOccurrenceService service;
    private final MarkOccurrenceMapper mapper;
    private final MarkMapper markMapper;
    private final MonumentMapper monumentMapper;
    private final DetectionService detectionService;
    private final MarkSearchService markSearchService;

    @GetMapping
    public Page<MarkOccurrenceDto> getMarkOccurrences(Pageable pageable) {
        return service.findAll(pageable)
                .map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarkOccurrenceDto> getMarkOccurrence(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-mark/{markId}")
    public Page<MarkOccurrenceListDto> getOccurrencesByMark(
            @PathVariable Long markId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "publishedAt"));
        return service.findByMarkId(markId, pageable)
                .map(mapper::toListDto);
    }

    @GetMapping("/map/by-mark/{markId}")
    public List<MarkOccurrenceMapDto> getOccurrencesForMapByMark(@PathVariable Long markId) {
        return service.findByMarkIdForMap(markId)
                .stream()
                .map(mapper::toMapDto)
                .toList();
    }

    @GetMapping("/latest")
    public List<MarkOccurrenceListDto> getLatestMarkOccurrences(
            @RequestParam(defaultValue = "6") int limit
    ) {
        int safeLimit = Math.min(limit, 50);
        return service.findLatest(safeLimit)
                .stream()
                .map(mapper::toListDto)
                .toList();
    }

    @GetMapping("/count-by-monument/{monumentId}")
    public ResponseEntity<Long> countByMonument(@PathVariable Long monumentId) {
        return ResponseEntity.ok(service.countByMonumentId(monumentId));
    }

    @GetMapping("/count-by-mark/{markId}")
    public ResponseEntity<Long> countByMark(@PathVariable Long markId) {
        return ResponseEntity.ok(service.countByMarkId(markId));
    }

    @GetMapping("/count-monuments-by-mark/{markId}")
    public ResponseEntity<Long> countMonumentsByMark(@PathVariable Long markId) {
        long count = service.countDistinctMonumentsByMarkId(markId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/by-monument/{monumentId}")
    public Page<MarkOccurrenceListDto> getOccurrencesByMonument(
            @PathVariable Long monumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "publishedAt"));
        return service.findByMonumentId(monumentId, pageable)
                .map(mapper::toListDto);
    }

    @GetMapping("/filters/marks-by-monument")
    public List<MarkDto> getAvailableMarksByMonument(@RequestParam Long monumentId) {
        return markMapper.toDto(service.findAvailableMarksByMonumentId(monumentId));
    }

    @GetMapping("/filters/monuments-by-mark")
    public List<MonumentListDto> getAvailableMonumentsByMark(@RequestParam Long markId) {
        return monumentMapper.toListDto(service.findAvailableMonumentsByMarkId(markId));
    }

    @GetMapping("/filter-by-mark-and-monument")
    public Page<MarkOccurrenceListDto> filterByMarkAndMonument(
            @RequestParam Long markId,
            @RequestParam Long monumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        return service.findByMarkIdAndMonumentId(markId, monumentId, pageable)
                .map(mapper::toListDto);
    }

    @PostMapping(value = "/search/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> searchByImage(@RequestParam("file") MultipartFile file) throws IOException {
        DetectionResult detectionResult = detectionService.detect(file.getInputStream(), file.getOriginalFilename());

        if (!detectionResult.isMasonMark()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<String> similarOccurrenceIds = markSearchService.searchOccurrences(VectorUtils.toFloatArray(detectionResult.embedding()));
        return ResponseEntity.ok(similarOccurrenceIds);
    }
}
