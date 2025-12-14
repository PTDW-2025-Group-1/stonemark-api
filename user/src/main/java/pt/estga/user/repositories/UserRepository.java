package pt.estga.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.user.entities.User;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.contacts WHERE u.id = :id")
    Optional<User> findByIdWithContacts(@Param("id") Long id);

    boolean existsByUsername(String username);

    void deleteAllByEnabledFalseAndCreatedAtBefore(Instant minus);
}
