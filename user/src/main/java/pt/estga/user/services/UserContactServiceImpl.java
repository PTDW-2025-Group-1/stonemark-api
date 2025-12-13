package pt.estga.user.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.repositories.UserContactRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserContactServiceImpl implements UserContactService {

    private final UserContactRepository repository;

    @Override
    public UserContact create(UserContact userContact) {
        log.info("Creating user contact: {}", userContact);
        return repository.save(userContact);
    }

    @Override
    public List<UserContact> findAllByUser(User user) {
        log.info("Finding user contacts by user: {}", user);
        return repository.findByUser(user);
    }

    @Override
    public Optional<UserContact> findPrimary(User user, ContactType contactType) {
        log.info("Finding primary user contact by user: {} and contact type: {}", user, contactType);
        return repository.findByUserAndTypeAndPrimaryContactAndVerified(user, contactType, true, true).stream().findFirst();
    }

    @Override
    public Optional<UserContact> findById(Long id) {
        log.info("Finding user contact by id: {}", id);
        return repository.findById(id);
    }

    @Override
    public Optional<UserContact> findByValue(String value) {
        log.info("Finding user contact by value: {}", value);
        return repository.findByValue(value);
    }

    @Override
    public Optional<UserContact> findByUserAndValue(User user, String value) {
        log.info("Finding user contact by user: {} and value: {}", user, value);
        return repository.findByUserAndValue(user, value);
    }

    @Override
    public boolean existsByValue(String value) {
        log.info("Checking if user contact exists by value: {}", value);
        return repository.existsByValue(value);
    }

    @Override
    public boolean existsByValueAndIsVerified(String value, boolean isVerified) {
        log.info("Checking if user contact exists by value: {} and isVerified: {}", value, isVerified);
        return repository.existsByValueAndVerified(value, isVerified);
    }

    @Override
    public UserContact update(UserContact userContact) {
        log.info("Updating user contact: {}", userContact);
        return repository.save(userContact);
    }

    @Override
    @Transactional
    public UserContact setAsPrimary(UserContact userContact) {
        log.info("Setting primary user contact: {}", userContact);

        if (!userContact.isVerified()) {
            throw new IllegalStateException("Contact must be verified before being set as primary.");
        }

        List<UserContact> contacts = repository.findByUser(userContact.getUser());

        for (UserContact contact : contacts) {
            contact.setPrimaryContact(contact.getId().equals(userContact.getId()));
        }

        repository.saveAll(contacts);

        return userContact;
    }


    @Override
    public void delete(UserContact userContact) {
        log.info("Deleting user contact: {}", userContact);
        repository.delete(userContact);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting user contact by id: {}", id);
        repository.deleteById(id);
    }
}
