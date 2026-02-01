package pt.estga.detection.model;

public record DetectionResult(
        boolean isMasonMark,
        float[] embedding
) { }
