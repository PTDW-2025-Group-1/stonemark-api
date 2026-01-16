package pt.estga.administrative.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ComponentScan("pt.estga.administrative")
@EnableJpaRepositories("pt.estga.administrative.repositories")
@EntityScan("pt.estga.administrative.entities")
@EnableAsync
public class AdministrativeModuleAutoConfiguration {
}
