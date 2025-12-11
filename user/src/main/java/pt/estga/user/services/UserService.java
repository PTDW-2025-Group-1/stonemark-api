package pt.estga.user.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.user.enums.Role;
import pt.estga.user.entities.User;

import java.time.Instant;
import java.util.Optional;

public interface UserService {

    Page<User> findAll(Pageable pageable);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    User create(User user);

    User update(User user);

    Optional<User> updateRole(User user, Role role);

    void deleteById(Long id);

    void deleteUnverifiedUsers(Instant minus);

}
