package pt.estga.territory.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.repositories.AdministrativeDivisionRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdministrativeDivisionService {

    private final AdministrativeDivisionRepository repository;

    public List<AdministrativeDivision> findAll() {
        return repository.findAll();
    }

    public Optional<AdministrativeDivision> findById(Long id) {
        return repository.findById(id);
    }

    public List<AdministrativeDivision> findAllByNameIn(Collection<String> names) {
        return repository.findAllByNameIn(names);
    }

    public List<AdministrativeDivision> findByOsmAdminLevel(int adminLevel) {
        return repository.findByOsmAdminLevel(adminLevel);
    }

    public List<AdministrativeDivision> findChildren(Long parentId) {
        return repository.findByParentId(parentId);
    }

    public Optional<AdministrativeDivision> findParent(Long childId) {
        return repository.findById(childId).map(AdministrativeDivision::getParent);
    }

    public List<AdministrativeDivision> findByCoordinates(double latitude, double longitude) {
        return repository.findByCoordinates(latitude, longitude);
    }

    public AdministrativeDivision create(AdministrativeDivision division) {
        return repository.save(division);
    }

    public AdministrativeDivision update(AdministrativeDivision division) {
        return repository.save(division);
    }

    public List<AdministrativeDivision> createOrUpdateAll(List<AdministrativeDivision> divisions) {
        return repository.saveAll(divisions);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
