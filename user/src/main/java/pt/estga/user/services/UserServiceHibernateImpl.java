package pt.estga.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.repositories.UserContactRepository;
import pt.estga.user.repositories.UserRepository;
import pt.estga.user.entities.User;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceHibernateImpl implements UserService {

    private final UserRepository repository;
    private final UserContactRepository contactRepository;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<User> findByContact(String contactValue) {
        return contactRepository.findByValue(contactValue)
                .map(UserContact::getUser);
    }

    @Override
    public Optional<User> findByContact(String contactValue, ContactType contactType) {
        return contactRepository.findByTypeAndValue(contactType, contactValue)
                .map(UserContact::getUser);
    }

    @Override
    public boolean existsByEmail(String email) {
        return contactRepository.findByTypeAndValue(ContactType.EMAIL, email).isPresent();
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
    public Optional<User> updateRole(User user, Role role) {
        user.setRole(role);
        return Optional.ofNullable(update(user));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByTelephone(String newTelephone) {
        return contactRepository.findByTypeAndValue(ContactType.TELEPHONE, newTelephone).isPresent();
    }

    @Override
    public Optional<String> getPrimaryTelephone(User user) {
        return user.getContacts().stream()
                .filter(c -> c.getType() == ContactType.TELEPHONE && c.isPrimary())
                .map(UserContact::getValue)
                .findFirst();
    }

    @Override
    public Optional<String> getPrimaryEmail(User user) {
        return user.getContacts().stream()
                .filter(c -> c.getType() == ContactType.EMAIL && c.isPrimary())
                .map(UserContact::getValue)
                .findFirst();
    }
}
