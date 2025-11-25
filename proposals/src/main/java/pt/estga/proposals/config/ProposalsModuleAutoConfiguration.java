package pt.estga.proposals.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("pt.estga.proposals")
@EnableJpaRepositories("pt.estga.proposals.repositories")
@EntityScan("pt.estga.proposals.entities")
public class ProposalsModuleAutoConfiguration {
}
