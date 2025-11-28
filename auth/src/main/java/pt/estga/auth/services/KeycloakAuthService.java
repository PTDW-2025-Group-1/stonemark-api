package pt.estga.auth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakAuthService {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getUserToken(String email, String password) {

        String url = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body =
                "grant_type=password" +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&username=" + email +
                        "&password=" + password;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        return response.getBody();
    }

    public List<String> extractRoles(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            String payload = new String(Base64.getDecoder().decode(parts[1]));

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> json = mapper.readValue(payload, Map.class);

            Map<String, Object> realmAccess = (Map<String, Object>) json.get("realm_access");

            return (List<String>) realmAccess.get("roles");

        } catch (Exception e) {
            return List.of();
        }
    }


}
