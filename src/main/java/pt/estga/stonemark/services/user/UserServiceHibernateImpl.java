package pt.estga.stonemark.services.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.dtos.account.ProfileUpdateRequestDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.Role;
import pt.estga.stonemark.exceptions.UserNotFoundException;
import pt.estga.stonemark.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceHibernateImpl implements UserService {

    private final UserRepository repository;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public User update(User user) {
        return repository.save(user);
    }

    @Override
    public void updateAccount(User user, ProfileUpdateRequestDto requestDto) {
        user.setFirstName(requestDto.firstName());
        user.setLastName(requestDto.lastName());
        user.setTelephone(requestDto.telephone());
        repository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.findByEmail(email).isPresent();
    }

    @Override
    public User updateRole(Long userId, Role newRole) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setRole(newRole);
        return repository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
