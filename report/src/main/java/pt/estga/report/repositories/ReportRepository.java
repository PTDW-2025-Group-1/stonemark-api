package pt.estga.report.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.shared.enums.TargetType;
import pt.estga.report.entities.Report;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, TargetType targetType);
}
