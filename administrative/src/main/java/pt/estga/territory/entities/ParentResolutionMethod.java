package pt.estga.territory.entities;

public enum ParentResolutionMethod {
    MANUAL(10), // Highest priority, for explicit manual assignments
    OSM_RELATION_MEMBERSHIP(9), // Future use: Explicit relation membership
    IMPORT_LEGACY(8), // Future use: Migrated data
    
    GEOMETRY_FULL_CONTAINMENT(3),
    GEOMETRY_HIGH_INTERSECTION(2),
    GEOMETRY_CENTROID_CONTAINMENT(1),
    
    ADMIN_LEVEL_HEURISTIC(0), // Lower priority
    COUNTRY_FALLBACK(-1); // Last resort

    private final int priority;

    ParentResolutionMethod(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
