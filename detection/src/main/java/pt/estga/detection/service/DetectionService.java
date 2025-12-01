package pt.estga.detection.service;

import pt.estga.detection.model.DetectionResult;

import java.io.InputStream;

public interface DetectionService {
    /**
     * Analyzes the provided image data to verify its content and extract a feature vector.
     *
     * @param imageInputStream The InputStream of the image to analyze.
     * @return A {@link DetectionResult} containing the outcome of the analysis.
     */
    DetectionResult detect(InputStream imageInputStream);
}
