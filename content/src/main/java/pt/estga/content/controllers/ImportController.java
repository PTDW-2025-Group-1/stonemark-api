package pt.estga.content.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.content.dtos.MonumentOverpassImportRequest;
import pt.estga.content.services.DivisionImportService;
import pt.estga.content.services.MonumentImportService;
import pt.estga.shared.dtos.MessageResponseDto;

@RestController
@RequestMapping("/api/v1/import")
@PreAuthorize("hasRole('MODERATOR')")
@RequiredArgsConstructor
@Tag(name = "Imports", description = "Endpoints for importing data.")
public class ImportController {

    private final DivisionImportService divisionImportService;
    private final MonumentImportService monumentImportService;

    @PostMapping("/divisions/overpass")
    public MessageResponseDto importDivisionsFromGeoJson(@RequestBody String geoJson) throws JsonProcessingException {
        int count = divisionImportService.overpass(geoJson);
        return new MessageResponseDto("Imported " + count + " divisions successfully.");
    }

    @PostMapping("/monuments/overpass")
    public MessageResponseDto importMonumentsFromOverpass(@RequestBody MonumentOverpassImportRequest request) throws JsonProcessingException {
        int count = monumentImportService.overpass(request.monumentData());
        return new MessageResponseDto("Imported " + count + " monuments successfully.");
    }
}
