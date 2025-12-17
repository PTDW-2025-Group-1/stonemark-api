package pt.estga.report.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.report.dtos.ReportRequestDto;
import pt.estga.report.dtos.ReportResponseDto;
import pt.estga.report.enums.ReportStatus;
import pt.estga.report.services.ReportService;
import pt.estga.user.entities.User;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for content reports and moderation.")
public class ReportController {

    private final ReportService service;

    @PostMapping
    public ReportResponseDto createReport(
            @AuthenticationPrincipal User user,
            @RequestBody ReportRequestDto dto
    ) {
        return service.createReport(user, dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public Page<ReportResponseDto> getAllReports(Pageable pageable) {
        return service.getAllReports(pageable);
    }

    @PatchMapping("/{reportId}/status")
    @PreAuthorize("hasRole('MODERATOR')")
    public ReportResponseDto updateStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status
    ) {
        return service.updateStatus(reportId, status);
    }
}
