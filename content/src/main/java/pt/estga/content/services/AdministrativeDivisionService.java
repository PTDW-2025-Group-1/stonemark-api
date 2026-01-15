package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.AdministrativeDivision;
import pt.estga.content.repositories.AdministrativeDivisionRepository;

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

    public Optional<AdministrativeDivision> findByName(String name) {
        return repository.findByName(name);
    }

    public Optional<AdministrativeDivision> findByNameAndAdminLevel(String name, int adminLevel) {
        return repository.findByNameAndAdminLevel(name, adminLevel);
    }

    public List<AdministrativeDivision> findAllByNameIn(Collection<String> names) {
        return repository.findAllByNameIn(names);
    }

    public List<AdministrativeDivision> findByAdminLevel(int adminLevel) {
        return repository.findByAdminLevel(adminLevel);
    }

    public List<AdministrativeDivision> findChildren(Long parentId, int childLevel) {
        return repository.findChildrenByParentId(parentId, childLevel);
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
