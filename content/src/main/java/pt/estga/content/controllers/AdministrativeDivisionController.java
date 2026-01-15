package pt.estga.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.content.dtos.AdministrativeDivisionDto;
import pt.estga.content.entities.AdministrativeDivision;
import pt.estga.content.mappers.AdministrativeDivisionMapper;
import pt.estga.content.services.AdministrativeDivisionService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/divisions")
@RequiredArgsConstructor
@Tag(name = "Administrative Divisions", description = "Endpoints for administrative divisions.")
public class AdministrativeDivisionController {

    private final AdministrativeDivisionService service;
    private final AdministrativeDivisionMapper mapper;

    @GetMapping("/districts")
    public ResponseEntity<List<AdministrativeDivisionDto>> getDistricts() {
        List<AdministrativeDivision> districts = service.findByAdminLevel(6);
        return ResponseEntity.ok(mapper.toDtoList(districts));
    }

    @GetMapping("/municipalities")
    public ResponseEntity<List<AdministrativeDivisionDto>> getMunicipalities() {
        List<AdministrativeDivision> municipalities = service.findByAdminLevel(7);
        return ResponseEntity.ok(mapper.toDtoList(municipalities));
    }

    @GetMapping("/parishes")
    public ResponseEntity<List<AdministrativeDivisionDto>> getParishes() {
        List<AdministrativeDivision> parishes = service.findByAdminLevel(8);
        return ResponseEntity.ok(mapper.toDtoList(parishes));
    }

    @GetMapping("/districts/{districtId}/municipalities")
    public ResponseEntity<List<AdministrativeDivisionDto>> getMunicipalitiesByDistrict(@PathVariable Long districtId) {
        List<AdministrativeDivision> municipalities = service.findChildren(districtId, 7);
        log.info("Municipalities: {}", municipalities);
        return ResponseEntity.ok(mapper.toDtoList(municipalities));
    }

    @GetMapping("/municipalities/{municipalityId}/parishes")
    public ResponseEntity<List<AdministrativeDivisionDto>> getParishesByMunicipality(@PathVariable Long municipalityId) {
        List<AdministrativeDivision> parishes = service.findChildren(municipalityId, 8);
        log.info("Parishes: {}", parishes);
        return ResponseEntity.ok(mapper.toDtoList(parishes));
    }

    @GetMapping("/coordinates")
    public ResponseEntity<List<AdministrativeDivisionDto>> getDivisionsByCoordinates(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        List<AdministrativeDivision> divisions = service.findByCoordinates(latitude, longitude);
        return ResponseEntity.ok(mapper.toDtoList(divisions));
    }
}
