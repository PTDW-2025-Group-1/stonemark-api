package pt.estga.user.services;

import pt.estga.user.entities.ServiceAccount;

import java.util.Optional;

public interface ServiceAccountService {

    ServiceAccount create(ServiceAccount serviceAccount);

    Optional<ServiceAccount> findById(Long id);

    Optional<ServiceAccount> findByName(String name);

    boolean existsById(Long id);

}
