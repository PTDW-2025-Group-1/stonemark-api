package pt.estga.boot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import pt.estga.shared.utils.SecurityUtils;
import pt.estga.user.entities.User;
import pt.estga.user.repositories.UserRepository;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Slf4j
public class JpaConfig {

    @Bean
    public AuditorAware<User> auditorProvider(UserRepository userRepository) {
        return () -> {
            try {
                return SecurityUtils.getCurrentUserId()
                        .flatMap(userRepository::findById);
            } catch (Exception e) {
                log.error("Error retrieving auditor user", e);
                return Optional.empty();
            }
        };
    }
}

