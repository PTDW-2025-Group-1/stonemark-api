package pt.estga.boot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pt.estga.security.models.UserPrincipal;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Slf4j
public class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            try {
                Authentication authentication =
                        SecurityContextHolder.getContext().getAuthentication();

                if (authentication == null || !authentication.isAuthenticated()) {
                    return Optional.empty();
                }

                Object principal = authentication.getPrincipal();

                if (principal instanceof UserPrincipal userPrincipal) {
                    return Optional.of(userPrincipal.getId());
                }

                return Optional.empty();
            } catch (Exception e) {
                log.error("Error retrieving auditor id", e);
                return Optional.empty();
            }
        };
    }
}
