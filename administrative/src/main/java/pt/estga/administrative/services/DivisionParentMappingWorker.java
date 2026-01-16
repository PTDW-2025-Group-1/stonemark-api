package pt.estga.administrative.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.administrative.entities.AdministrativeDivision;
import pt.estga.administrative.repositories.AdministrativeDivisionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DivisionParentMappingWorker {

    private final AdministrativeDivisionRepository repository;

    @Async
    @Transactional
    public void calculateParents() {
        log.info("Starting parent-child relationship calculation for administrative divisions...");

        List<AdministrativeDivision> allDivisions = repository.findAll();
        Map<Integer, List<AdministrativeDivision>> divisionsByLevel = allDivisions.stream()
                .collect(Collectors.groupingBy(AdministrativeDivision::getAdminLevel));

        List<AdministrativeDivision> divisionsToUpdate = new ArrayList<>();

        // Process Municipalities (adminLevel 7) to find District (adminLevel 6)
        // Note: Autonomous Regions (Azores/Madeira) are now imported as level 6, so this logic covers them too.
        List<AdministrativeDivision> municipalities = divisionsByLevel.getOrDefault(7, Collections.emptyList());
        for (AdministrativeDivision municipality : municipalities) {
            repository.findParentByGeometry(municipality.getId(), 6)
                    .ifPresentOrElse(parent -> {
                        municipality.setParent(parent);
                        divisionsToUpdate.add(municipality);
                    }, () -> log.warn("Municipality '{}' (ID: {}) has no parent district found.", municipality.getName(), municipality.getId()));
        }

        // Process Parishes (adminLevel 8) to find Municipality (adminLevel 7) parents
        List<AdministrativeDivision> parishes = divisionsByLevel.getOrDefault(8, Collections.emptyList());
        for (AdministrativeDivision parish : parishes) {
            repository.findParentByGeometry(parish.getId(), 7)
                    .ifPresentOrElse(parent -> {
                        parish.setParent(parent);
                        divisionsToUpdate.add(parish);
                    }, () -> log.warn("Parish '{}' (ID: {}) has no parent municipality found.", parish.getName(), parish.getId()));
        }

        if (!divisionsToUpdate.isEmpty()) {
            repository.saveAll(divisionsToUpdate);
            log.info("Updated {} administrative divisions with parent relationships.", divisionsToUpdate.size());
        } else {
            log.info("No parent-child relationships found or updated.");
        }
    }
}
