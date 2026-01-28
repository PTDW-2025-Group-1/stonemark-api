package pt.estga.decision.enums;

public enum DecisionOutcome {
    ACCEPT,
    REJECT,
    INCONCLUSIVE;

    public boolean isFinal() {
        return this == ACCEPT || this == REJECT;
    }
}
