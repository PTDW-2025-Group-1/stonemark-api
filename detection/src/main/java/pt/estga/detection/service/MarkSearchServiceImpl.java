package pt.estga.detection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.Mark;
import pt.estga.content.repositories.MarkRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarkSearchServiceImpl implements MarkSearchService {

    private final MarkRepository markRepository;
    private final ObjectMapper objectMapper; // This might not be needed anymore in this class

    private static final double SIMILARITY_THRESHOLD = 0.8;

    @Override
    public List<String> searchMarks(List<Double> embeddedVector) {
        List<Mark> allMarks = markRepository.findAll();

        return allMarks.stream()
                .filter(mark -> mark.getEmbedding() != null && !mark.getEmbedding().isEmpty())
                .map(mark -> {
                    List<Double> markVector = mark.getEmbedding();
                    double similarity = calculateCosineSimilarity(embeddedVector, markVector);
                    if (similarity >= SIMILARITY_THRESHOLD) {
                        return String.valueOf(mark.getId());
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size() || vector1.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            magnitude1 += Math.pow(vector1.get(i), 2);
            magnitude2 += Math.pow(vector2.get(i), 2);
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }
}
