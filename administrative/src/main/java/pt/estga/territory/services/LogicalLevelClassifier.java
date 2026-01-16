package pt.estga.territory.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.entities.DivisionFlag;
import pt.estga.territory.entities.LogicalLevel;

import java.util.HashSet;

@Component
@Slf4j
public class LogicalLevelClassifier {

    // Configurable thresholds (Portugal defaults)
    private static final double THRESHOLD_REGION = 500.0;
    private static final double THRESHOLD_DISTRICT = 100.0;
    private static final double THRESHOLD_MUNICIPALITY = 10.0;
    private static final double THRESHOLD_TINY = 10.0; // Used for mis-tag correction

    public void classify(AdministrativeDivision div) {
        // Default strategy: OSM admin_level mapping
        classifyByOsmLevel(div);

        // Country-specific refinements
        if ("PT".equals(div.getCountryCode())) {
            applyPortugalHeuristics(div);
        }
        // Future: else if ("ES".equals(div.getCountryCode())) { ... }
    }

    private void classifyByOsmLevel(AdministrativeDivision div) {
        int osmLevel = div.getOsmAdminLevel() != null ? div.getOsmAdminLevel() : -1;
        
        LogicalLevel classifiedLevel;
        double confidence = switch (osmLevel) {
            case 2 -> {
                classifiedLevel = LogicalLevel.COUNTRY;
                yield 1.0;
            }
            case 4 -> {
                classifiedLevel = LogicalLevel.AUTONOMOUS_REGION;
                yield 0.9;
            }
            case 6 -> {
                classifiedLevel = LogicalLevel.DISTRICT;
                yield 0.9;
            }
            case 7 -> {
                classifiedLevel = LogicalLevel.MUNICIPALITY;
                yield 0.9;
            }
            case 8 -> {
                classifiedLevel = LogicalLevel.PARISH;
                yield 0.9;
            }
            default -> {
                classifiedLevel = LogicalLevel.OTHER;
                yield 0.5;
            }
        };

        div.setLogicalLevel(classifiedLevel);
        div.setLogicalLevelConfidence(confidence);
    }

    private void applyPortugalHeuristics(AdministrativeDivision div) {
        double area = div.getAreaKm2() != null ? div.getAreaKm2() : 0.0;
        int osmLevel = div.getOsmAdminLevel() != null ? div.getOsmAdminLevel() : -1;
        LogicalLevel currentLevel = div.getLogicalLevel();
        double currentConfidence = div.getLogicalLevelConfidence();

        LogicalLevel areaBasedLevel;
        if (area > THRESHOLD_REGION) {
            areaBasedLevel = LogicalLevel.AUTONOMOUS_REGION;
        } else if (area > THRESHOLD_DISTRICT) {
            areaBasedLevel = LogicalLevel.DISTRICT;
        } else if (area > THRESHOLD_MUNICIPALITY) {
            areaBasedLevel = LogicalLevel.MUNICIPALITY;
        } else {
            areaBasedLevel = LogicalLevel.PARISH;
        }

        // Heuristic 1: Disambiguate Level 4 (District vs Region)
        if (osmLevel == 4 && areaBasedLevel == LogicalLevel.AUTONOMOUS_REGION) {
            if (currentLevel != LogicalLevel.AUTONOMOUS_REGION) {
                addFlag(div, DivisionFlag.ADMIN_LEVEL_MISMATCH);
                log.debug("PT Heuristic: Correcting {} from {} to REGION based on area.", div.getId(), currentLevel);
            }
            div.setLogicalLevel(LogicalLevel.AUTONOMOUS_REGION);
            div.setLogicalLevelConfidence(Math.max(currentConfidence, 0.95));
        } 
        // Heuristic 2: Flag significant mismatches
        else if (currentLevel != areaBasedLevel && currentLevel != LogicalLevel.OTHER) {
            addFlag(div, DivisionFlag.ADMIN_LEVEL_MISMATCH);
            log.debug("PT Heuristic Warning: {} is classified as {} but area suggests {}.", 
                      div.getId(), currentLevel, areaBasedLevel);
        }
        
        // Heuristic 3: Tiny area correction
        if (osmLevel == 4 && area < THRESHOLD_TINY) {
             div.setLogicalLevel(LogicalLevel.OTHER);
             addFlag(div, DivisionFlag.TINY_AREA);
             div.setLogicalLevelConfidence(Math.min(currentConfidence, 0.3));
        }
    }

    private void addFlag(AdministrativeDivision div, DivisionFlag flag) {
        if (div.getFlags() == null) {
            div.setFlags(new HashSet<>());
        }
        div.getFlags().add(flag);
    }
}
