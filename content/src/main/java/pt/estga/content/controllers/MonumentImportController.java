package pt.estga.content.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.content.dtos.MonumentDto;
import pt.estga.stonemark.mappers.MonumentMapper;
import pt.estga.stonemark.services.content.MonumentImportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/import/monuments")
@RequiredArgsConstructor
public class MonumentImportController {

    private final MonumentImportService monumentImportService;
    private final MonumentMapper monumentMapper;

    @PostMapping("/overpass")
    public List<MonumentDto> importFromOverpass(@RequestBody String geoJson) throws JsonProcessingException {
        return monumentImportService.overpass(geoJson).stream().map(monumentMapper::toDto).toList();
    }
}
