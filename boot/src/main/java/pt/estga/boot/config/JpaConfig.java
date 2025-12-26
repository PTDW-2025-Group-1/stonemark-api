package pt.estga.boot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import pt.estga.shared.utils.SecurityUtils;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Slf4j
public class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            try {
                return SecurityUtils.getCurrentUserId();
            } catch (Exception e) {
                log.error("Error retrieving auditor user", e);
                return Optional.empty();
            }
        };
    }
}
