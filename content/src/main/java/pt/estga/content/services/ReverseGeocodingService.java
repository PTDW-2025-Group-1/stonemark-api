package pt.estga.content.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.estga.content.dtos.GeocodingResultDto;

@Service
@RequiredArgsConstructor
public class ReverseGeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public GeocodingResultDto reverseGeocode(double lat, double lon) {
        // Added extratags=1 to get more details like description, wikipedia, etc.
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&addressdetails=1&extratags=1&lat="
                + lat + "&lon=" + lon;

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);

        if (response == null) return null;

        String address = response.path("display_name").asText(null);
        
        JsonNode addr = response.path("address");

        String street = addr.has("road") ? addr.get("road").asText()
                : addr.has("pedestrian") ? addr.get("pedestrian").asText()
                : addr.has("street") ? addr.get("street").asText()
                : null;
        
        String houseNumber = addr.has("house_number") ? addr.get("house_number").asText() : null;

        String name = null;
        if (response.has("name")) {
            name = response.get("name").asText();
        } else {
            if (addr.has("amenity")) name = addr.get("amenity").asText();
            else if (addr.has("building")) name = addr.get("building").asText();
            else if (addr.has("tourism")) name = addr.get("tourism").asText();
            else if (addr.has("historic")) name = addr.get("historic").asText();
        }

        String description = null;
        JsonNode extratags = response.path("extratags");
        if (extratags.has("description")) {
            description = extratags.get("description").asText();
        } else if (extratags.has("wikipedia")) {
            // Fallback to wikipedia link if description is missing, or maybe just note it
            // For now, let's just take description if available.
        }

        return GeocodingResultDto.builder()
                .name(name)
                .address(address)
                .description(description)
                .street(street)
                .houseNumber(houseNumber)
                .build();
    }
}
