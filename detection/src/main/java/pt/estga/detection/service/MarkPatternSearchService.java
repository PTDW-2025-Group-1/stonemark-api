package pt.estga.detection.service;

import pt.estga.detection.model.MarkPattern;

import java.util.List;

public interface MarkPatternSearchService {

    float[] getVectorFromImage(byte[] imageData);

    List<MarkPattern> searchSimilarPatterns(float[] vector);
}
