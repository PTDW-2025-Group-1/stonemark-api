package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.content.repositories.MarkOccurrenceRepository;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.content.repositories.projections.MarkSimilarityProjection;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarkSearchServiceImpl implements MarkSearchService {

    private final MarkRepository markRepository;
    private final MarkOccurrenceRepository markOccurrenceRepository;

    private static final double SIMILARITY_THRESHOLD = 0.8;

    @Override
    public List<String> searchMarks(float[] embeddedVector) {
        if (embeddedVector == null || embeddedVector.length == 0) {
            return List.of();
        }

        List<MarkSimilarityProjection> results = markRepository.findSimilarMarks(embeddedVector);

        return results.stream()
                .filter(result -> result.getSimilarity() >= SIMILARITY_THRESHOLD)
                .map(result -> String.valueOf(result.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> searchOccurrences(float[] embeddedVector) {
        if (embeddedVector == null || embeddedVector.length == 0) {
            return List.of();
        }

        List<MarkSimilarityProjection> results = markOccurrenceRepository.findSimilarOccurrences(embeddedVector);

        return results.stream()
                .filter(result -> result.getSimilarity() >= SIMILARITY_THRESHOLD)
                .map(result -> String.valueOf(result.getId()))
                .collect(Collectors.toList());
    }
}
