package pt.estga.bots.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("pt.estga.bots")
@EnableJpaRepositories("pt.estga.bots.repositories")
@EntityScan("pt.estga.bots.entities")
public class BotsModuleAutoConfiguration {
}
