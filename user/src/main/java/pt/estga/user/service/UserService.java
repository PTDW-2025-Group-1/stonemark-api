package pt.estga.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface UserService {

    Page<User> findAll(Pageable pageable);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User create(User user);

    User update(User user);

    void deleteById(Long id);

}
