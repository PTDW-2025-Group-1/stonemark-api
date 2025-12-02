package pt.estga.detection.service;

import pt.estga.detection.model.DetectionResult;

import java.io.InputStream;

public interface DetectionService {
    /**
     * Analyzes the provided image data to verify its content and extract a feature embedding.
     *
     * @param imageInputStream The InputStream of the image to analyze.
     * @param originalFilename The original filename of the image.
     * @return A {@link DetectionResult} containing the outcome of the analysis.
     */
    DetectionResult detect(InputStream imageInputStream, String originalFilename);
}
