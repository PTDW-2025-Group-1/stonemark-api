package pt.estga.content.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ReverseGeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAddress(double lat, double lon) {
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&addressdetails=1&lat="
                + lat + "&lon=" + lon;

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);

        if (response == null) return null;

        return response.path("display_name").asText(null);
    }

    public String getCity(double lat, double lon) {
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&addressdetails=1&lat="
                + lat + "&lon=" + lon;

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);

        if (response == null) return null;

        JsonNode addr = response.path("address");

        return addr.has("city") ? addr.get("city").asText()
                : addr.has("town") ? addr.get("town").asText()
                : addr.has("village") ? addr.get("village").asText()
                : null;
    }
}
