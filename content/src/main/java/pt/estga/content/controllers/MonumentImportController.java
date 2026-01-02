package pt.estga.content.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.content.dtos.MonumentOverpassImportRequest;
import pt.estga.content.services.MonumentImportService;
import pt.estga.shared.dtos.MessageResponseDto;

@RestController
@RequestMapping("/api/v1/monuments/import/")
@PreAuthorize("hasRole('MODERATOR')")
@RequiredArgsConstructor
public class MonumentImportController {

    private final MonumentImportService monumentImportService;

    @PostMapping("/overpass")
    public MessageResponseDto importFromOverpass(@RequestBody MonumentOverpassImportRequest request) throws JsonProcessingException {
        int count = monumentImportService.overpass(request.monumentData(), request.division());
        return new MessageResponseDto("Imported " + count + " monuments successfully.");
    }
}
