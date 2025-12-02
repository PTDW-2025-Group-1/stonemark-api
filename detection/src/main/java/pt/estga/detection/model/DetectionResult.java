package pt.estga.detection.model;

import java.util.List;

public record DetectionResult(
        boolean isMasonMark,
        List<Double> embedding
) { }
