package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.administrative.services.DivisionImportService;
import pt.estga.content.services.MonumentImportService;
import pt.estga.shared.dtos.MessageResponseDto;

import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/import")
@PreAuthorize("hasRole('MODERATOR')")
@RequiredArgsConstructor
@Tag(name = "Imports", description = "Endpoints for importing data.")
public class ImportController {

    private final DivisionImportService divisionImportService;
    private final MonumentImportService monumentImportService;

    @PostMapping("/divisions/pbf")
    public MessageResponseDto importDivisionsFromPbf(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        int count;
        try (InputStream is = file.getInputStream()) {
            count = divisionImportService.importFromPbf(is);
        }

        return new MessageResponseDto(
                "Administrative divisions fully replaced. Imported " + count + " entries."
        );
    }

    @PostMapping("/monuments/geojson")
    public MessageResponseDto importMonumentsFromGeoJson(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        int count;
        try (InputStream is = file.getInputStream()) {
            count = monumentImportService.importFromGeoJson(is);
        }

        return new MessageResponseDto("Imported " + count + " monuments successfully.");
    }
}
