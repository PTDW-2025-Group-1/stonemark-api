package pt.estga.user.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.user.enums.Role;
import pt.estga.user.entities.User;
import pt.estga.user.enums.ContactType;

import java.util.Optional;

public interface UserService {

    Page<User> findAll(Pageable pageable);

    Optional<User> findById(Long id);

    Optional<User> findByContact(String contactValue);

    boolean existsByContactValue(String contactValue);

    User create(User user);

    User update(User user);

    Optional<User> updateRole(User user, Role role);

    void deleteById(Long id);

}
