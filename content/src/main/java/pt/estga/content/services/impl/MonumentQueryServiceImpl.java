package pt.estga.content.services.impl;

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
import pt.estga.content.repositories.MonumentQueryRepository;
import pt.estga.content.services.MonumentQueryService;
import pt.estga.territory.entities.AdministrativeDivision;
import pt.estga.territory.services.AdministrativeDivisionService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonumentQueryServiceImpl implements MonumentQueryService {

    private final MonumentQueryRepository repository;
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
    public Optional<Monument> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Monument> findByCoordinatesInRange(double latitude, double longitude, double range) {
        return repository.findByCoordinatesInRange(latitude, longitude, range);
    }

    @Override
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
    @Cacheable("popularMonuments")
    public List<Monument> findPopular(int limit) {
        return repository.findPopular(PageRequest.of(0, limit));
    }
}
