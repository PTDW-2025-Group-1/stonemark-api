package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MonumentRepository;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.services.AdministrativeDivisionService;

import java.util.List;
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
    public Monument create(Monument monument) {
        setParishByCoordinates(monument);
        return repository.save(monument);
    }

    @Override
    public Monument update(Monument monument) {
        setParishByCoordinates(monument);
        return repository.save(monument);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private void setParishByCoordinates(Monument m) {
        if (m.getLatitude() != null && m.getLongitude() != null && (m.getParish() == null || m.getMunicipality() == null || m.getDistrict() == null)) {
            List<AdministrativeDivision> divisions = administrativeDivisionService.findByCoordinates(m.getLatitude(), m.getLongitude());
            for (AdministrativeDivision division : divisions) {
                switch (division.getOsmAdminLevel()) {
                    case 6:
                        if (m.getDistrict() == null) {
                            m.setDistrict(division);
                        }
                        break;
                    case 7:
                        if (m.getMunicipality() == null) {
                            m.setMunicipality(division);
                        }
                        break;
                    case 8:
                        if (m.getParish() == null) {
                            m.setParish(division);
                        }
                        break;
                }
            }
        }
    }
}
