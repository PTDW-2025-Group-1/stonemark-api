package pt.estga.stonemark.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.stonemark.dtos.ChangePasswordRequestDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.Role;

import java.security.Principal;
import java.util.Optional;

public interface UserService {

    Page<User> findAll(Pageable pageable);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User create(User user);

    User update(User user);

    User updateRole(Long userId, Role newRole);

    void changePassword(ChangePasswordRequestDto request, Principal connectedUser);

    void deleteById(Long id);

}
