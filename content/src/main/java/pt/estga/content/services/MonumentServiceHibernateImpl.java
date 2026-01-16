package pt.estga.content.services;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pt.estga.administrative.entities.AdministrativeDivision;
import pt.estga.administrative.services.AdministrativeDivisionService;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MonumentRepository;

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
        Optional<Monument> monument = repository.findById(id);
        monument.ifPresent(this::setParishByCoordinates);
        return monument;
    }

    @Override
    public List<Monument> findByCoordinatesInRange(double latitude, double longitude, double range) {
        return repository.findByLatitudeBetweenAndLongitudeBetween(latitude - range, latitude + range, longitude - range, longitude + range);
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
    public Page<Monument> findByDivisionId(Long divisionId, Pageable pageable) {
        Optional<AdministrativeDivision> division = administrativeDivisionService.findById(divisionId);
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
        if (m.getParish() == null && m.getLatitude() != null && m.getLongitude() != null) {
            List<AdministrativeDivision> divisions = administrativeDivisionService.findByCoordinates(m.getLatitude(), m.getLongitude());
            divisions.stream()
                    .filter(d -> d.getAdminLevel() == 8)
                    .findFirst()
                    .ifPresent(m::setParish);
        }
    }
}
