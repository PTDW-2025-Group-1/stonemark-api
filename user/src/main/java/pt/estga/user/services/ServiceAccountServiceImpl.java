package pt.estga.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.user.entities.ServiceAccount;
import pt.estga.user.repositories.ServiceAccountRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceAccountServiceImpl implements ServiceAccountService {

    private final ServiceAccountRepository serviceAccountRepository;

    @Override
    @Transactional
    public ServiceAccount create(ServiceAccount serviceAccount) {
        return serviceAccountRepository.save(serviceAccount);
    }

    @Override
    public Optional<ServiceAccount> findById(Long id) {
        return serviceAccountRepository.findById(id);
    }

    @Override
    public Optional<ServiceAccount> findByName(String name) {
        return serviceAccountRepository.findByName(name);
    }

    @Override
    public boolean existsById(Long id) {
        return serviceAccountRepository.existsById(id);
    }
}
