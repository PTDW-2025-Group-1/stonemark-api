package pt.estga.stonemark.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi auth() {
        return GroupedOpenApi.builder()
                .group("Authentication")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi account() {
        return GroupedOpenApi.builder()
                .group("Account")
                .pathsToMatch("/api/v1/account/**")
                .build();
    }

    @Bean
    public GroupedOpenApi users() {
        return GroupedOpenApi.builder()
                .group("Users")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi monuments() {
        return GroupedOpenApi.builder()
                .group("Monuments")
                .pathsToMatch("/api/v1/monuments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi marks() {
        return GroupedOpenApi.builder()
                .group("Marks")
                .pathsToMatch("/api/v1/marks/**")
                .build();
    }

    @Bean
    public GroupedOpenApi markOccurrences() {
        return GroupedOpenApi.builder()
                .group("Mark Occurrences")
                .pathsToMatch("/api/v1/mark-occurrences/**")
                .build();
    }

    @Bean
    public GroupedOpenApi contactRequests() {
        return GroupedOpenApi.builder()
                .group("Contact Requests")
                .pathsToMatch("/api/v1/contact-requests/**")
                .build();
    }

    @Bean
    public OpenAPI securedOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("StoneMark API")
                        .version("v1.0")
                        .description("REST API for StoneMark application."));
    }
}
