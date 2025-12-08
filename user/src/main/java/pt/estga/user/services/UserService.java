package pt.estga.user.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.user.enums.Role;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface UserService {

    Page<User> findAll(Pageable pageable);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByTelephone(String telephone);

    User create(User user);

    User update(User user);

    Optional<User> updateRole(User user, Role role);

    void deleteById(Long id);

    boolean existsByTelephone(String newTelephone);

    Optional<String> getPrimaryTelephone(User user);

    Optional<String> getPrimaryEmail(User user);

}
