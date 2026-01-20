package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MonumentRepository;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.services.AdministrativeDivisionService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonumentServiceHibernateImpl implements MonumentService {

    private final MonumentRepository repository;
    private final AdministrativeDivisionService administrativeDivisionService;

    @Override
    public Page<Monument> findAll(Pageable pageable) {
        return repository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Monument> findAllWithDivisions(Pageable pageable) {
        return repository.findAllWithDivisions(pageable);
    }

    @Override
    public Page<Monument> findAllWithDivisionsManagement(Pageable pageable) {
        return repository.findAllWithDivisionsAdmin(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Monument> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Monument> findByCoordinatesInRange(double latitude, double longitude, double range) {
        return repository.findByCoordinatesInRange(latitude, longitude, range);
    }

    public List<Monument> findLatest(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findByActiveTrue(pageable).getContent();
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public Page<Monument> searchByName(String query, Pageable pageable) {
        return repository.findByNameContainingIgnoreCaseAndActiveTrue(query, pageable);
    }

    @Override
    public Page<Monument> findByPolygon(String geoJson, Pageable pageable) {
        return repository.findByPolygon(geoJson, pageable);
    }

    @Override
    public Page<Monument> findByDivisionId(Long id, Pageable pageable) {
        Optional<AdministrativeDivision> division = administrativeDivisionService.findById(id);
        if (division.isPresent()) {
            Geometry geometry = division.get().getGeometry();
            if (geometry != null) {
                return repository.findByGeometry(geometry, pageable);
            }
        }
        return Page.empty(pageable);
    }

    @Override
    @Transactional
    public Monument create(Monument monument) {
        setDivisions(monument);
        Monument savedMonument = repository.save(monument);
        updateCounters(null, savedMonument);
        return savedMonument;
    }

    @Override
    @Transactional
    public Monument update(Monument monument) {
        Optional<Monument> existingMonument = repository.findById(monument.getId());
        setDivisions(monument);
        Monument updatedMonument = repository.save(monument);
        existingMonument.ifPresent(value -> updateCounters(value, updatedMonument));
        return updatedMonument;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(monument -> {
            updateCounters(monument, null);
            repository.deleteById(id);
        });
    }

    @Override
    @Cacheable("popularMonuments")
    public List<Monument> findPopular(int limit) {
        return repository.findPopular(PageRequest.of(0, limit));
    }

    private void updateCounters(Monument oldMonument, Monument newMonument) {
        AdministrativeDivision oldParish = oldMonument != null ? oldMonument.getParish() : null;
        AdministrativeDivision newParish = newMonument != null ? newMonument.getParish() : null;
        updateDivisionCounter(oldParish, newParish);

        AdministrativeDivision oldMunicipality = oldMonument != null ? oldMonument.getMunicipality() : null;
        AdministrativeDivision newMunicipality = newMonument != null ? newMonument.getMunicipality() : null;
        updateDivisionCounter(oldMunicipality, newMunicipality);

        AdministrativeDivision oldDistrict = oldMonument != null ? oldMonument.getDistrict() : null;
        AdministrativeDivision newDistrict = newMonument != null ? newMonument.getDistrict() : null;
        updateDivisionCounter(oldDistrict, newDistrict);
    }

    private void updateDivisionCounter(AdministrativeDivision oldDivision, AdministrativeDivision newDivision) {
        if (Objects.equals(oldDivision, newDivision)) {
            return;
        }
        if (oldDivision != null) {
            administrativeDivisionService.decrementMonumentsCount(oldDivision.getId());
        }
        if (newDivision != null) {
            administrativeDivisionService.incrementMonumentsCount(newDivision.getId());
        }
    }

    private void setDivisions(Monument m) {
        setDivisions(m, administrativeDivisionService);
    }

    static void setDivisions(Monument m, AdministrativeDivisionService administrativeDivisionService) {
        if (m.getLatitude() != null && m.getLongitude() != null) {
            List<AdministrativeDivision> divisions = administrativeDivisionService.findByCoordinates(m.getLatitude(), m.getLongitude());
            m.setParish(null);
        m.setMunicipality(null);
        m.setDistrict(null);
            for (AdministrativeDivision division : divisions) {
                switch (division.getOsmAdminLevel()) {
                    case 6 -> m.setDistrict(division);
                    case 7 -> m.setMunicipality(division);
                    case 8 -> m.setParish(division);
                }
            }
        }
    }
}
