package pt.estga.boot.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.repositories.UserRepository;

import java.time.Instant;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SystemUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public CommandLineRunner initSystemUser() {
        return args -> {
            transactionTemplate.execute(status -> {
                if (userRepository.findById(0L).isEmpty()) {
                    log.info("Initializing SYSTEM user with ID 0");
                    
                    // NOTE: We use a native query here because Hibernate's @GeneratedValue
                    // ignores manually set IDs on the entity. To force the ID to be 0 (for System Auditing),
                    // we must bypass Hibernate and insert directly into the database.
                    entityManager.createNativeQuery(
                            "INSERT INTO _user (id, first_name, last_name, username, password, role, enabled, account_locked, tfa_method, created_at) " +
                            "VALUES (0, 'System', 'Administrator', 'system', ?1, ?2, true, false, ?3, ?4)")
                            .setParameter(1, passwordEncoder.encode("system_password_change_me"))
                            .setParameter(2, Role.ADMIN.name())
                            .setParameter(3, TfaMethod.NONE.name())
                            .setParameter(4, Instant.now())
                            .executeUpdate();
                            
                    log.info("SYSTEM user initialized successfully.");
                }
                return null;
            });
        };
    }
}
