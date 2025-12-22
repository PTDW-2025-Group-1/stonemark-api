package pt.estga.security.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("pt.estga.security")
@EnableJpaRepositories("pt.estga.security.repositories")
@EntityScan("pt.estga.security.entities")
public class SecurityModuleAutoConfiguration {
}
