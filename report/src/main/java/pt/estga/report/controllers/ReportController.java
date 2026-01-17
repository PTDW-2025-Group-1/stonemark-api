package pt.estga.report.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.report.dtos.ReportRequestDto;
import pt.estga.report.dtos.ReportResponseDto;
import pt.estga.report.enums.ReportStatus;
import pt.estga.report.services.ReportService;
import pt.estga.shared.models.AppPrincipal;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for content reports and moderation.")
public class ReportController {

    private final ReportService service;
    private final UserService userService;

    @PostMapping
    public ReportResponseDto createReport(
            @AuthenticationPrincipal AppPrincipal principal,
            @Valid
            @RequestBody ReportRequestDto dto
    ) {
        User user = userService.findById(principal.getId()).orElseThrow();
        return service.createReport(user, dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Page<ReportResponseDto>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(service.getAllReports(pageable));
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
