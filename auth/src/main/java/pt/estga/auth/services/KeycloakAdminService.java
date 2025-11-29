package pt.estga.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.estga.user.entities.User;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    @Value("${keycloak.auth-server-url}")
    private String authUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getAdminToken() {
        String url = authUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body =
                "grant_type=password" +
                        "&client_id=admin-cli" +
                        "&username=" + adminUsername +
                        "&password=" + adminPassword;

        HttpEntity<String> req = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.postForEntity(url, req, Map.class);

        return (String) res.getBody().get("access_token");
    }


    public String createUserInKeycloak(String email, String password, String firstName, String lastName) {
        String token = getAdminToken();

        String url = authUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
        {
          "username": "%s",
          "email": "%s",
          "firstName": "%s",
          "lastName": "%s",
          "enabled": true,
          "emailVerified": true,
          "credentials": [{
          "type": "password",
          "value": "%s",
          "temporary": false
          }]
        }
        """.formatted(email, email, firstName, lastName, password);

        HttpEntity<String> req = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, req, Void.class);

        String location = response.getHeaders().getLocation().toString();
        String id = location.substring(location.lastIndexOf('/') + 1);

        return id;
    }


    public void updateUserInKeycloak(String keycloakUserId, User user) {
        String token = getAdminToken();

        String url = authUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
        {
          "username": "%s",
          "email": "%s",
          "firstName": "%s",
          "lastName": "%s",
          "enabled": %s
        }
        """.formatted(
                user.getEmail(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled()
        );

        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), Void.class);
    }


}

