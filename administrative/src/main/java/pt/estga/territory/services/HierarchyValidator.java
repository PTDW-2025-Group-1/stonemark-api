package pt.estga.territory.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.entities.DivisionFlag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class HierarchyValidator {

    public boolean validate(List<AdministrativeDivision> divisions) {
        log.info("Starting hierarchy validation...");
        int cycles = 0;
        int invalidArea = 0;
        int logicalLevelSkips = 0;
        int geometryMismatches = 0;
        boolean overallValidation = true;
        
        for (AdministrativeDivision div : divisions) {
            // Check for cycles
            if (hasCycle(div)) {
                cycles++;
                log.error("Validation Error: Cycle detected for division: {}", div.getId());
                overallValidation = false;
            }
            
            if (div.getParent() != null) {
                // Check parent area (Warning only)
                double parentArea = div.getParent().getAreaKm2() != null ? div.getParent().getAreaKm2() : 0.0;
                double childArea = div.getAreaKm2() != null ? div.getAreaKm2() : 0.0;
                
                if (parentArea <= childArea) {
                    invalidArea++;
                    addFlag(div, DivisionFlag.INVALID_PARENT_AREA);
                    log.warn("Validation Warning: Potential invalid parent area for division {}: Parent {} is smaller ({} vs {}).", 
                            div.getId(), div.getParent().getId(), parentArea, childArea);
                }

                // Check for logical level skips (Error)
                if (div.getLogicalLevel() != null && div.getParent().getLogicalLevel() != null) {
                    if (!div.getParent().getLogicalLevel().isParentOf(div.getLogicalLevel())) {
                        logicalLevelSkips++;
                        log.error("Validation Error: Logical level skip detected for division {}: {} -> {}", 
                                div.getId(), div.getParent().getLogicalLevel(), div.getLogicalLevel());
                        overallValidation = false;
                    }
                }

                // Check geometric containment post-assignment (Warning only)
                // Note: Geometry is not absolute truth, so this is a warning, not a failure.
                if (div.getGeometry() != null && div.getParent().getGeometry() != null) {
                    if (!div.getParent().getGeometry().contains(div.getGeometry())) {
                        geometryMismatches++;
                        addFlag(div, DivisionFlag.GEOMETRY_PARENT_MISMATCH);
                        log.warn("Validation Warning: Geometry mismatch for division {}: Parent {} does not fully contain it.", 
                                div.getId(), div.getParent().getId());
                    }
                }
            }
        }
        
        log.info("Validation complete. Cycles: {}, Suspicious Parent Areas: {}, Logical Level Skips: {}, Geometry Mismatches: {}", 
                 cycles, invalidArea, logicalLevelSkips, geometryMismatches);
        return overallValidation;
    }

    private boolean hasCycle(AdministrativeDivision startNode) {
        AdministrativeDivision current = startNode.getParent();
        Set<String> visited = new HashSet<>();
        visited.add(startNode.getId());
        
        int depth = 0;
        while (current != null && depth < 100) { // Max depth to prevent infinite loops on very long chains
            if (visited.contains(current.getId())) {
                return true;
            }
            visited.add(current.getId());
            current = current.getParent();
            depth++;
        }
        return false;
    }

    private void addFlag(AdministrativeDivision div, DivisionFlag flag) {
        if (div.getFlags() == null) {
            div.setFlags(new HashSet<>());
        }
        div.getFlags().add(flag);
    }
}
