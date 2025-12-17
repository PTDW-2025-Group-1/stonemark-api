package pt.estga.report.dtos;

import pt.estga.shared.enums.TargetType;
import pt.estga.report.enums.ReportReason;
import pt.estga.report.enums.ReportStatus;

import java.time.Instant;

public record ReportResponseDto(
        Long id,
        Long userId,
        Long targetId,
        TargetType targetType,
        ReportReason reason,
        String description,
        ReportStatus status,
        Instant createdAt
) {}
