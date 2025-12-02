package pt.estga.detection.service;

import java.util.List;

public interface MarkSearchService {
    /**
     * Searches for marks that match the given embedded embedding.
     *
     * @param embeddedVector The embedded embedding to search for.
     * @return A list of identifiers of the marks that match the pattern.
     */
    List<String> searchMarks(List<Double> embeddedVector);
}
