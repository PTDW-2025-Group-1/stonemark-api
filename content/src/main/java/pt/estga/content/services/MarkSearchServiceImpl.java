package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.content.repositories.projections.MarkSimilarityProjection;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarkSearchServiceImpl implements MarkSearchService {

    private final MarkRepository markRepository;

    private static final double SIMILARITY_THRESHOLD = 0.8;

    @Override
    public List<String> searchMarks(double[] embeddedVector) {
        if (embeddedVector == null || embeddedVector.length == 0) {
            return List.of();
        }

        String vectorString = Arrays.toString(embeddedVector);

        List<MarkSimilarityProjection> results = markRepository.findSimilarMarks(vectorString);

        return results.stream()
                .filter(result -> result.getSimilarity() >= SIMILARITY_THRESHOLD)
                .map(result -> String.valueOf(result.getId()))
                .collect(Collectors.toList());
    }
}
