package pt.estga.detection.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("pt.estga.detection")
@EnableJpaRepositories("pt.estga.detection.repositories")
@EntityScan("pt.estga.detection.entities")
public class DetectionModuleAutoConfiguration {
}
