package pt.estga.detection.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.estga.detection.dto.DetectionRequestDto;
import pt.estga.detection.dto.VectorResponseDto;
import pt.estga.detection.model.MarkPattern;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarkPatternSearchServiceImpl implements MarkPatternSearchService {

    private final RestTemplate restTemplate;

    @Value("${vision.server.url}")
    private String detectionServerUrl;

    @Override
    public float[] getVectorFromImage(byte[] imageData) {
        DetectionRequestDto requestDto = new DetectionRequestDto(imageData);
        VectorResponseDto responseDto = restTemplate.postForObject(detectionServerUrl + "/vectorize", requestDto, VectorResponseDto.class);

        if (responseDto == null || responseDto.vector() == null) {
            return new float[0];
        }

        // Convert List<Double> to float[]
        float[] vector = new float[responseDto.vector().size()];
        for (int i = 0; i < responseDto.vector().size(); i++) {
            vector[i] = responseDto.vector().get(i).floatValue();
        }
        return vector;
    }

    @Override
    public List<MarkPattern> searchSimilarPatterns(float[] vector) {
        // Here you would call another endpoint on the server to find similar patterns
        // For now, this is just a placeholder.
        return Collections.emptyList();
    }
}
