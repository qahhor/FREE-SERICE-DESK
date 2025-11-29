package com.servicedesk.monolith.analytics.controller;

import com.servicedesk.monolith.analytics.dto.DashboardMetrics;
import com.servicedesk.monolith.analytics.dto.ReportRequest;
import com.servicedesk.monolith.analytics.service.AnalyticsService;
import com.servicedesk.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardMetrics>> getDashboardMetrics(
            @RequestParam(required = false) String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardMetrics metrics = analyticsService.getDashboardMetrics(projectId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/agents")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAgentPerformance(
            @RequestParam(required = false) String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Map<String, Object>> performance = analyticsService.getAgentPerformance(projectId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(performance));
    }

    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTeamPerformance(
            @RequestParam(required = false) String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Map<String, Object>> performance = analyticsService.getTeamPerformance(projectId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(performance));
    }

    @GetMapping("/sla")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlaReport(
            @RequestParam(required = false) String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> report = analyticsService.getSlaReport(projectId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PostMapping("/reports/generate")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportRequest request) {
        byte[] report = analyticsService.generateReport(request);

        String contentType = switch (request.getFormat()) {
            case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "csv" -> "text/csv";
            default -> MediaType.APPLICATION_JSON_VALUE;
        };

        String filename = "report_" + request.getType().name().toLowerCase() + "_" +
                request.getStartDate() + "_" + request.getEndDate() + "." + request.getFormat();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(contentType))
                .body(report);
    }

    @GetMapping("/realtime")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRealtimeMetrics(
            @RequestParam(required = false) String projectId) {

        // Get metrics for today
        LocalDate today = LocalDate.now();
        DashboardMetrics metrics = analyticsService.getDashboardMetrics(projectId, today, today);

        Map<String, Object> realtime = Map.of(
                "ticketsCreatedToday", metrics.getTicketsCreatedToday(),
                "ticketsResolvedToday", metrics.getTicketsResolvedToday(),
                "openTickets", metrics.getOpenTickets(),
                "pendingTickets", metrics.getPendingTickets(),
                "activeAgents", metrics.getActiveAgents(),
                "averageResponseTime", metrics.getAverageResponseTime()
        );

        return ResponseEntity.ok(ApiResponse.success(realtime));
    }
}
