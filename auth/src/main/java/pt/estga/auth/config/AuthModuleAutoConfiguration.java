package pt.estga.auth.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("pt.estga.auth")
@EnableJpaRepositories("pt.estga.auth.repositories")
@EntityScan("pt.estga.auth.entities")
public class AuthModuleAutoConfiguration {
}
