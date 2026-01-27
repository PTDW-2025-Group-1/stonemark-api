package pt.estga.content.services;

import java.util.List;

public interface MarkSearchService {
    List<String> searchMarks(double[] embeddedVector);
}
