package pt.estga.territory.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.entities.DivisionFlag;
import pt.estga.territory.entities.LogicalLevel;
import pt.estga.territory.entities.ParentResolutionMethod;
import pt.estga.territory.repositories.AdministrativeDivisionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DivisionParentMappingWorker {

    private final AdministrativeDivisionRepository repository;
    private final LogicalLevelClassifier logicalLevelClassifier;
    private final HierarchyValidator hierarchyValidator;

    /**
     * Helper class to store candidate parent information along with the resolution method.
     */
    private static class CandidateInfo {
        AdministrativeDivision parent;
        ParentResolutionMethod method;
        double areaKm2; // For sorting

        public CandidateInfo(AdministrativeDivision parent, ParentResolutionMethod method) {
            this.parent = parent;
            this.method = method;
            this.areaKm2 = parent.getAreaKm2() != null ? parent.getAreaKm2() : Double.MAX_VALUE;
        }

        // Comparator for sorting candidates:
        // 1. By resolution method priority (Higher priority first)
        // 2. By smallest area as a tie-breaker
        public static Comparator<CandidateInfo> getComparator() {
            return Comparator
                    .<CandidateInfo>comparingInt(ci -> -ci.method.getPriority()) // Higher priority first (descending)
                    .thenComparingDouble(ci -> ci.areaKm2); // Then smallest area
        }
    }

    // In-memory result structure to separate calculation from persistence
    private static class ParentAssignmentResult {
        Map<String, AdministrativeDivision> proposedParents = new HashMap<>();
        Map<String, ParentResolutionMethod> proposedResolutionMethods = new HashMap<>();
        Map<String, Double> confidenceScores = new HashMap<>();
        Map<String, Set<DivisionFlag>> newFlags = new HashMap<>();
    }

    public void calculateParents() {
        calculateParents(true, true);
    }

    @Transactional
    public void calculateParents(boolean recomputeLogicalLevels, boolean recomputeGeometryRelations) {
        long startTime = System.currentTimeMillis();
        log.info("Starting parent-child relationship calculation (Levels: {}, Geometry: {})...", 
                 recomputeLogicalLevels, recomputeGeometryRelations);

        List<AdministrativeDivision> allDivisions = repository.findAll();
        
        // Phase 1: Pre-classify logical levels
        if (recomputeLogicalLevels) {
            log.info("Classifying logical levels for all divisions...");
            for (AdministrativeDivision div : allDivisions) {
                // Clear non-manual flags before re-classification
                if (div.getFlags() != null) {
                    div.setFlags(div.getFlags().stream()
                            .filter(f -> f == DivisionFlag.MANUAL_PARENT_OVERRIDE)
                            .collect(Collectors.toSet()));
                }
                logicalLevelClassifier.classify(div);
            }
        }

        // Phase 2 & 3: Parent Assignment (In-Memory)
        ParentAssignmentResult result = new ParentAssignmentResult();
        
        if (recomputeGeometryRelations) {
            computeParentAssignments(allDivisions, result);
        }

        // Phase 4: Validation & Persistence (Transactional)
        applyAndPersist(allDivisions, result, recomputeGeometryRelations, startTime);
    }

    private void computeParentAssignments(List<AdministrativeDivision> allDivisions, ParentAssignmentResult result) {
        log.info("Building spatial index and preparing geometries...");
        STRtree index = new STRtree();
        Map<String, PreparedGeometry> preparedGeometries = new HashMap<>();

        for (AdministrativeDivision div : allDivisions) {
            if (div.getGeometry() != null) {
                index.insert(div.getGeometry().getEnvelopeInternal(), div);
                preparedGeometries.put(div.getId(), PreparedGeometryFactory.prepare(div.getGeometry()));
            }
        }
        index.build();

        log.info("Assigning parents to divisions...");
        for (AdministrativeDivision child : allDivisions) {
            // Skip parent calculation for divisions with manual override
            if (child.getFlags() != null && child.getFlags().contains(DivisionFlag.MANUAL_PARENT_OVERRIDE)) {
                log.debug("Skipping parent calculation for {} due to MANUAL_PARENT_OVERRIDE flag.", child.getId());
                continue;
            }

            if (child.getGeometry() == null || child.getLogicalLevel() == null) {
                addFlagToResult(result, child.getId(), DivisionFlag.ORPHAN);
                result.confidenceScores.put(child.getId(), 0.0);
                continue;
            }
            
            // Query spatial index for potential parents
            List<CandidateInfo> candidates = queryCandidates(index, child, preparedGeometries);
            
            AdministrativeDivision bestParent = null;
            ParentResolutionMethod resolutionMethod = null;

            if (!candidates.isEmpty()) {
                // Filter candidates by logical level adjacency
                List<CandidateInfo> levelAdjacentCandidates = candidates.stream()
                    .filter(ci -> ci.parent.getLogicalLevel() != null && ci.parent.getLogicalLevel().isParentOf(child.getLogicalLevel()))
                    .collect(Collectors.toList());

                if (!levelAdjacentCandidates.isEmpty()) {
                    // Sort level-adjacent candidates
                    levelAdjacentCandidates.sort(CandidateInfo.getComparator());
                    
                    bestParent = levelAdjacentCandidates.get(0).parent;
                    resolutionMethod = levelAdjacentCandidates.get(0).method;

                    // Check for multi-parent candidates among level-adjacent ones
                    if (levelAdjacentCandidates.size() > 1) {
                         CandidateInfo secondBest = levelAdjacentCandidates.get(1);
                         // If the second best has the same resolution method priority and is very close in area (within 10%)
                         if (secondBest.method.getPriority() == resolutionMethod.getPriority()) {
                             double area1 = bestParent.getAreaKm2() != null ? bestParent.getAreaKm2() : 0.0;
                             double area2 = secondBest.areaKm2;
                             
                             if (area2 > 0 && (area1 / area2) > 0.9) {
                                 addFlagToResult(result, child.getId(), DivisionFlag.MULTI_PARENT_CANDIDATE);
                                 // Ambiguous parents should block assignment
                                 bestParent = null;
                                 resolutionMethod = null;
                             }
                         }
                    }
                } else {
                    // No level-adjacent parent found, but other candidates exist. Flag as potential skip.
                    addFlagToResult(result, child.getId(), DivisionFlag.LOGICAL_LEVEL_SKIP_CANDIDATE);
                }
            }

            if (bestParent != null) {
                result.proposedParents.put(child.getId(), bestParent);
                result.proposedResolutionMethods.put(child.getId(), resolutionMethod);
                
                // Calculate confidence score
                boolean contains = false;
                if (preparedGeometries.containsKey(bestParent.getId())) {
                    contains = preparedGeometries.get(bestParent.getId()).contains(child.getGeometry());
                } else {
                    // Fallback if prepared geometry is missing (should be rare)
                    // Treat as unknown/false to be safe, or check raw geometry carefully
                    // Here we assume false to avoid false positives on invalid geometries
                    contains = false; 
                }

                if (contains) {
                    result.confidenceScores.put(child.getId(), 1.0);
                } else {
                    double ratio = intersectionRatio(bestParent, child);
                    result.confidenceScores.put(child.getId(), Math.min(1.0, ratio));
                }
            } else {
                // Mark as ORPHAN if it should have a parent but doesn't (e.g. not a country)
                if (child.getLogicalLevel() != LogicalLevel.COUNTRY) {
                     addFlagToResult(result, child.getId(), DivisionFlag.ORPHAN);
                }
                result.confidenceScores.put(child.getId(), 0.0);
            }
        }
    }


    protected void applyAndPersist(List<AdministrativeDivision> allDivisions, ParentAssignmentResult result, boolean recomputeGeometryRelations, long startTime) {
        // Apply proposed parents to the actual objects
        for (AdministrativeDivision div : allDivisions) {
            // Apply flags
            if (result.newFlags.containsKey(div.getId())) {
                if (div.getFlags() == null) div.setFlags(new HashSet<>());
                div.getFlags().addAll(result.newFlags.get(div.getId()));
            }

            if (result.proposedParents.containsKey(div.getId())) {
                div.setParent(result.proposedParents.get(div.getId()));
                div.setParentResolutionMethod(result.proposedResolutionMethods.get(div.getId()));
                if (result.confidenceScores.containsKey(div.getId())) {
                    div.setConfidenceScore(result.confidenceScores.get(div.getId()));
                }
            } else {
                // Only clear parent if it wasn't manually overridden AND wasn't manual resolution
                if (div.getFlags() == null || !div.getFlags().contains(DivisionFlag.MANUAL_PARENT_OVERRIDE)) {
                    if (div.getParentResolutionMethod() != ParentResolutionMethod.MANUAL) {
                        // Only clear if we actually ran a strategy that could have found a parent
                        if (recomputeGeometryRelations) {
                            div.setParent(null);
                            div.setParentResolutionMethod(null);
                            div.setConfidenceScore(0.0);
                        }
                    }
                }
            }
        }

        // Phase 4: Validation on the in-memory hierarchy
        log.info("Validating proposed hierarchy...");
        boolean validationPassed = hierarchyValidator.validate(allDivisions);

        if (validationPassed) {
            // Phase 5: Persist changes if validation passes
            log.info("Hierarchy validation passed. Persisting changes...");
            repository.saveAll(allDivisions);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Updated {} administrative divisions in {} ms.", allDivisions.size(), duration);
            
            // Phase 6: Debug & Tooling - Dump Hierarchy
            logHierarchy(allDivisions);
        } else {
            log.error("Hierarchy validation failed. No changes were persisted.");
            throw new IllegalStateException("Hierarchy validation failed. Check logs for details.");
        }
    }

    private List<CandidateInfo> queryCandidates(STRtree index, AdministrativeDivision child, Map<String, PreparedGeometry> preparedGeometries) {
        List<CandidateInfo> candidates = new ArrayList<>();
        List<?> potentialParents = index.query(child.getGeometry().getEnvelopeInternal());
        
        for (Object obj : potentialParents) {
            AdministrativeDivision parent = (AdministrativeDivision) obj;
            
            // Self check
            if (parent.getId().equals(child.getId())) continue;
            
            // Cheap geometric check: Envelope containment
            if (!parent.getGeometry().getEnvelopeInternal().contains(child.getGeometry().getEnvelopeInternal())) {
                continue;
            }

            ParentResolutionMethod bestGeometricMethod = null;

            // Check prepared geometry for full containment
            boolean contains = false;
            if (preparedGeometries.containsKey(parent.getId())) {
                contains = preparedGeometries.get(parent.getId()).contains(child.getGeometry());
            } else {
                // Fallback: treat as false/unknown to be safe
                contains = false;
            }

            // Order geometric checks by confidence (most confident first)
            if (contains) {
                bestGeometricMethod = ParentResolutionMethod.GEOMETRY_FULL_CONTAINMENT;
            } else if (intersectionRatio(parent, child) >= 0.95) {
                bestGeometricMethod = ParentResolutionMethod.GEOMETRY_HIGH_INTERSECTION;
            }

            if (bestGeometricMethod != null) {
                candidates.add(new CandidateInfo(parent, bestGeometricMethod));
            }
        }
        return candidates;
    }

    private double intersectionRatio(AdministrativeDivision parent, AdministrativeDivision child) {
        try {
            // Optimization: Check intersection before calculating area
            if (!parent.getGeometry().intersects(child.getGeometry())) return 0.0;
            
            Geometry intersection = parent.getGeometry().intersection(child.getGeometry());
            // Cache child area if possible, but for now just compute it.
            // Cap ratio at 1.0
            double ratio = intersection.getArea() / child.getGeometry().getArea();
            return Math.min(1.0, ratio);
        } catch (Exception e) {
            log.warn("Geometry intersection error between {} and {}: {}", parent.getId(), child.getId(), e.getMessage());
            return 0.0;
        }
    }

    private void addFlagToResult(ParentAssignmentResult result, String divId, DivisionFlag flag) {
        result.newFlags.computeIfAbsent(divId, k -> new HashSet<>()).add(flag);
    }

    private void logHierarchy(List<AdministrativeDivision> allDivisions) {
        log.info("Dumping hierarchy tree...");
        Map<String, List<AdministrativeDivision>> childrenMap = allDivisions.stream()
            .filter(d -> d.getParent() != null)
            .collect(Collectors.groupingBy(d -> d.getParent().getId()));
            
        List<AdministrativeDivision> roots = allDivisions.stream()
            .filter(d -> d.getParent() == null)
            .sorted(Comparator.comparingDouble(d -> d.getAreaKm2() != null ? -d.getAreaKm2() : 0.0)) // Largest first
            .toList();
            
        for (AdministrativeDivision root : roots) {
            printNode(root, childrenMap, 0);
        }
    }
    
    private void printNode(AdministrativeDivision node, Map<String, List<AdministrativeDivision>> childrenMap, int depth) {
        String indent = "  ".repeat(depth);
        log.info("{}{} ({}) - ID: {} (OSM: {} {}) [Conf: {}] [ParentMethod: {}]", 
                 indent, node.getName(), node.getLogicalLevel(), node.getId(), node.getOsmType(), node.getOsmId(),
                 node.getConfidenceScore(), node.getParentResolutionMethod());
        
        List<AdministrativeDivision> children = childrenMap.getOrDefault(node.getId(), Collections.emptyList());
        children.sort(Comparator.comparing(AdministrativeDivision::getName));
        
        for (AdministrativeDivision child : children) {
            printNode(child, childrenMap, depth + 1);
        }
    }
}
