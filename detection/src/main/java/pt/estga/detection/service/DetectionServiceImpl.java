package pt.estga.detection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pt.estga.detection.dto.DetectionResponseDto;
import pt.estga.detection.model.DetectionResult;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionServiceImpl implements DetectionService {

    private final RestTemplate restTemplate;

    @Value("${vision.server.url}")
    private String detectionServerUrl;

    @Override
    public DetectionResult detect(InputStream imageInputStream, String originalFilename) {
        log.info("Starting detection process for file: {}", originalFilename);

        // Determine the MediaType based on the filename
        MediaType fileMediaType = getMediaType(originalFilename);
        log.info("Determined MediaType for file {}: {}", originalFilename, fileMediaType);

        // Use MultipartBodyBuilder to construct the request body
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new InputStreamResource(imageInputStream))
               .filename(originalFilename != null ? originalFilename : "image.bin") // Ensure filename is not null
               .contentType(fileMediaType);

        // Build the HttpEntity for the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity =
                new HttpEntity<>(builder.build(), headers);

        log.info("Sending request to detection server at: {}", detectionServerUrl + "/process");

        ResponseEntity<DetectionResponseDto> response = restTemplate.postForEntity(detectionServerUrl + "/process", requestEntity, DetectionResponseDto.class);

        DetectionResponseDto responseDto = response.getBody();

        if (responseDto == null) {
            log.warn("Detection response body is null. Returning a failed detection result.");
            return new DetectionResult(false, null);
        }

        log.info("Detection process completed. Mason mark detected: {}", responseDto.isMasonMark());
        return new DetectionResult(responseDto.isMasonMark(), responseDto.embedding());
    }

    @NotNull
    private static MediaType getMediaType(String originalFilename) {
        MediaType fileMediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (originalFilename != null) {
            String lowerCaseFilename = originalFilename.toLowerCase();
            if (lowerCaseFilename.endsWith(".jpg") || lowerCaseFilename.endsWith(".jpeg")) {
                fileMediaType = MediaType.IMAGE_JPEG;
            } else if (lowerCaseFilename.endsWith(".png")) {
                fileMediaType = MediaType.IMAGE_PNG;
            }
            // Add other image types if necessary
        }
        return fileMediaType;
    }
}
