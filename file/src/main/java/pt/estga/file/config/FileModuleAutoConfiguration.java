package pt.estga.file.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("pt.estga.file")
@EnableJpaRepositories("pt.estga.file.repositories")
@EntityScan("pt.estga.file.entities")
public class FileModuleAutoConfiguration {
}
