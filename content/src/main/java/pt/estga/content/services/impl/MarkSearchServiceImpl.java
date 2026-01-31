package pt.estga.content.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.content.repositories.MarkOccurrenceQueryRepository;
import pt.estga.content.repositories.MarkQueryRepository;
import pt.estga.content.repositories.projections.MarkSimilarityProjection;
import pt.estga.content.services.MarkSearchService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarkSearchServiceImpl implements MarkSearchService {

    private final MarkQueryRepository markQueryRepository;
    private final MarkOccurrenceQueryRepository markOccurrenceQueryRepository;

    private static final double SIMILARITY_THRESHOLD = 0.8;

    @Override
    public List<String> searchMarks(float[] embeddedVector) {
        if (embeddedVector == null || embeddedVector.length == 0) {
            return List.of();
        }

        List<MarkSimilarityProjection> results = markQueryRepository.findSimilarMarks(embeddedVector);

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

        List<MarkSimilarityProjection> results = markOccurrenceQueryRepository.findSimilarOccurrences(embeddedVector);

        return results.stream()
                .filter(result -> result.getSimilarity() >= SIMILARITY_THRESHOLD)
                .map(result -> String.valueOf(result.getId()))
                .collect(Collectors.toList());
    }
}
