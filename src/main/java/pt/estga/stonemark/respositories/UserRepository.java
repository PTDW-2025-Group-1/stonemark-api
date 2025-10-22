package pt.estga.stonemark.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.stonemark.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}
