package pt.estga.report.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.estga.shared.enums.TargetType;
import pt.estga.report.enums.ReportReason;

public record ReportRequestDto(
        @NotNull
        Long targetId,
        @NotNull
        TargetType targetType,
        @NotNull
        ReportReason reason,
        @Size(max = 1000)
        String description
) {}

