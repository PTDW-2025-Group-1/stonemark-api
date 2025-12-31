package pt.estga.proposal.enums;

public enum DecisionOutcome {
    ACCEPT,
    REJECT,
    INCONCLUSIVE;

    public boolean isFinal() {
        return this == ACCEPT || this == REJECT;
    }
}
