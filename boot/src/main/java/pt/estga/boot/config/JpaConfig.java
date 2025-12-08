package pt.estga.boot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pt.estga.user.entities.User;
import pt.estga.user.services.AuditorUserService;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Slf4j
public class JpaConfig {

    @Bean
    public AuditorAware<User> auditorProvider(AuditorUserService auditorUserService) {
        return () -> {
            try {
                return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                        .filter(Authentication::isAuthenticated)
                        .map(Authentication::getName)
                        .flatMap(auditorUserService::findByEmail);
            } catch (Exception e) {
                log.error("Error retrieving auditor user: {}", e.getMessage(), e);
                return Optional.empty();
            }
        };
    }
}
