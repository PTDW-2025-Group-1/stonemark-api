package pt.estga.stonemark.services;

import pt.estga.stonemark.dtos.ChangePasswordRequestDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.Role;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    User save(User user);

    boolean deleteById(Long id);

    boolean existsByEmail(String email);

    boolean updateRole(Long userId, Role newRole);

    boolean changePassword(ChangePasswordRequestDto request, Principal connectedUser);

}
