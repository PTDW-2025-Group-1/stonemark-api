package pt.estga.territory.entities;

public enum LogicalLevel {
    COUNTRY,
    AUTONOMOUS_REGION,
    DISTRICT,
    MUNICIPALITY,
    PARISH,
    OTHER;

    /**
     * Checks if this logical level can be a direct parent of the given child logical level.
     * Enforces level adjacency in the hierarchy (e.g., REGION -> DISTRICT).
     *
     * @param childLevel The logical level of the potential child.
     * @return true if this level can be a parent of the child level, false otherwise.
     */
    public boolean isParentOf(LogicalLevel childLevel) {
        if (this.equals(childLevel)) {
            return false; // A level cannot be its own parent
        }

        return switch (this) {
            case COUNTRY -> childLevel == AUTONOMOUS_REGION;
            case AUTONOMOUS_REGION -> childLevel == DISTRICT;
            case DISTRICT -> childLevel == MUNICIPALITY;
            case MUNICIPALITY -> childLevel == PARISH;
            default -> false; // Parish and OTHER cannot be parents in this hierarchy
        };
    }
}
