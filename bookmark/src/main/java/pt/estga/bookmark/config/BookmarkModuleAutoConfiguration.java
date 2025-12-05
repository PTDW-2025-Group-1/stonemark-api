package pt.estga.bookmark.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("pt.estga.bookmark")
@EnableJpaRepositories("pt.estga.bookmark.repositories")
@EntityScan("pt.estga.bookmark.entities")
public class BookmarkModuleAutoConfiguration {
}
