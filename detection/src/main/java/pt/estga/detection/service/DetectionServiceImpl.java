package pt.estga.detection.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pt.estga.detection.dto.DetectionResponseDto;
import pt.estga.detection.model.DetectionResult;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DetectionServiceImpl implements DetectionService {

    private final RestTemplate restTemplate;

    @Value("${vision.server.url}")
    private String detectionServerUrl;

    @Override
    public DetectionResult detect(InputStream imageInputStream) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new InputStreamResource(imageInputStream));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<DetectionResponseDto> response = restTemplate.postForEntity(detectionServerUrl + "/detect", requestEntity, DetectionResponseDto.class);

        DetectionResponseDto responseDto = response.getBody();

        if (responseDto == null) {
            // Handle the case where the detection server returns an empty response
            return new DetectionResult(false, null);
        }

        return new DetectionResult(responseDto.isMasonMark(), responseDto.vector());
    }
}
