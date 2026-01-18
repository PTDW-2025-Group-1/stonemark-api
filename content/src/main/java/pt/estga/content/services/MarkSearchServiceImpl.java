package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.content.repositories.MarkRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarkSearchServiceImpl implements MarkSearchService {

    private final MarkRepository markRepository;

    private static final double SIMILARITY_THRESHOLD = 0.8;

    @Override
    public List<String> searchMarks(List<Double> embeddedVector) {
        if (embeddedVector == null || embeddedVector.isEmpty()) {
            return List.of();
        }

        String vectorString = embeddedVector.toString();

        List<Object[]> results = markRepository.findSimilarMarks(vectorString);

        return results.stream()
                .filter(result -> {
                    double similarity = ((Number) result[1]).doubleValue();
                    return similarity >= SIMILARITY_THRESHOLD;
                })
                .map(result -> String.valueOf(result[0]))
                .collect(Collectors.toList());
    }
}
