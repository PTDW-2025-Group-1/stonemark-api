package pt.estga.content.services;

import java.util.List;

public interface MarkSearchService {
    List<String> searchMarks(float[] embeddedVector);
    List<String> searchOccurrences(float[] embeddedVector);
}
