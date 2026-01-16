package pt.estga.administrative.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.administrative.dto.AdministrativeDivisionDto;
import pt.estga.administrative.entities.AdministrativeDivision;
import pt.estga.administrative.mappers.AdministrativeDivisionMapper;
import pt.estga.administrative.services.AdministrativeDivisionService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/divisions")
@RequiredArgsConstructor
@Tag(name = "Administrative Divisions", description = "Endpoints for administrative divisions.")
public class AdministrativeDivisionController {

    private final AdministrativeDivisionService service;
    private final AdministrativeDivisionMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<AdministrativeDivisionDto> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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
        List<AdministrativeDivision> municipalities = service.findChildren(districtId);
        log.info("Municipalities: {}", municipalities);
        return ResponseEntity.ok(mapper.toDtoList(municipalities));
    }

    @GetMapping("/municipalities/{municipalityId}/parishes")
    public ResponseEntity<List<AdministrativeDivisionDto>> getParishesByMunicipality(@PathVariable Long municipalityId) {
        List<AdministrativeDivision> parishes = service.findChildren(municipalityId);
        log.info("Parishes: {}", parishes);
        return ResponseEntity.ok(mapper.toDtoList(parishes));
    }

    @GetMapping("/municipalities/{municipalityId}/district")
    public ResponseEntity<AdministrativeDivisionDto> getDistrictByMunicipality(@PathVariable Long municipalityId) {
        return service.findParent(municipalityId)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/parishes/{parishId}/municipality")
    public ResponseEntity<AdministrativeDivisionDto> getMunicipalityByParish(@PathVariable Long parishId) {
        return service.findParent(parishId)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
