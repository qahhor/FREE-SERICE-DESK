package com.servicedesk.monolith.ticket.controller;

import com.servicedesk.monolith.ticket.dto.ChangeRequestDto;
import com.servicedesk.monolith.ticket.entity.*;
import com.servicedesk.monolith.ticket.service.ChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/changes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChangeController {

    private final ChangeService changeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<ChangeRequestDto.ListResponse>> getAllChanges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) ChangeRequest.ChangeStatus status) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Page<ChangeRequest> changes = status != null
                ? changeService.getChangesByStatus(status, PageRequest.of(page, size, sort))
                : changeService.getAllChanges(PageRequest.of(page, size, sort));

        return ResponseEntity.ok(changes.map(this::toListResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ChangeRequestDto.Response> getChange(@PathVariable UUID id) {
        return changeService.getChange(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ChangeRequestDto.Response> createChange(
            @Valid @RequestBody ChangeRequestDto.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        ChangeRequest change = changeService.createChange(request, currentUser);
        return ResponseEntity.ok(toResponse(change));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ChangeRequestDto.Response> updateChange(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeRequestDto.UpdateRequest request) {
        ChangeRequest change = changeService.updateChange(id, request);
        return ResponseEntity.ok(toResponse(change));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ChangeRequestDto.Response> submitForApproval(@PathVariable UUID id) {
        ChangeRequest change = changeService.submitForApproval(id);
        return ResponseEntity.ok(toResponse(change));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChangeRequestDto.ApprovalResponse> processApproval(
            @PathVariable UUID id,
            @RequestBody ChangeRequestDto.ApprovalDecision decision,
            @AuthenticationPrincipal User currentUser) {
        ChangeApproval approval = changeService.processApproval(id, UUID.fromString(currentUser.getId()), decision);
        return ResponseEntity.ok(toApprovalResponse(approval));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ChangeRequestDto.Response> updateStatus(
            @PathVariable UUID id,
            @RequestBody ChangeRequestDto.StatusUpdate statusUpdate) {
        ChangeRequest change = changeService.updateStatus(id, statusUpdate.getStatus(), statusUpdate.getNotes());
        return ResponseEntity.ok(toResponse(change));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ChangeRequestDto.Response> completeReview(
            @PathVariable UUID id,
            @RequestBody ChangeRequestDto.ReviewRequest review) {
        ChangeRequest change = changeService.completeReview(id, review);
        return ResponseEntity.ok(toResponse(change));
    }

    @GetMapping("/pending-approval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ChangeRequestDto.ListResponse>> getPendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ChangeRequest> changes = changeService.getPendingApprovals(PageRequest.of(page, size));
        return ResponseEntity.ok(changes.map(this::toListResponse));
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<ChangeRequestDto.CalendarEvent>> getChangeCalendar(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<ChangeRequest> changes = changeService.getScheduledChanges(start, end);
        List<ChangeRequestDto.CalendarEvent> events = changes.stream()
                .map(this::toCalendarEvent)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ChangeRequestDto.ChangeMetrics> getMetrics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();
        return ResponseEntity.ok(changeService.getMetrics(startDate, endDate));
    }

    // Mappers
    private ChangeRequestDto.Response toResponse(ChangeRequest c) {
        return ChangeRequestDto.Response.builder()
                .id(c.getId())
                .changeNumber(c.getChangeNumber())
                .title(c.getTitle())
                .description(c.getDescription())
                .justification(c.getJustification())
                .changeType(c.getChangeType())
                .category(c.getCategory())
                .priority(c.getPriority())
                .riskLevel(c.getRiskLevel())
                .impact(c.getImpact())
                .status(c.getStatus())
                .requester(toUserSummary(c.getRequester()))
                .assignee(c.getAssignee() != null ? toUserSummary(c.getAssignee()) : null)
                .team(c.getTeam() != null ? toTeamSummary(c.getTeam()) : null)
                .scheduledStart(c.getScheduledStart())
                .scheduledEnd(c.getScheduledEnd())
                .actualStart(c.getActualStart())
                .actualEnd(c.getActualEnd())
                .implementationPlan(c.getImplementationPlan())
                .rollbackPlan(c.getRollbackPlan())
                .testPlan(c.getTestPlan())
                .communicationPlan(c.getCommunicationPlan())
                .reviewNotes(c.getReviewNotes())
                .success(c.getSuccess())
                .projectId(c.getProject() != null ? c.getProject().getId() : null)
                .approvals(c.getApprovals().stream().map(this::toApprovalResponse).collect(Collectors.toList()))
                .linkedTicketsCount(c.getLinkedTickets().size())
                .affectedAssetsCount(c.getAffectedAssets().size())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ChangeRequestDto.ListResponse toListResponse(ChangeRequest c) {
        return ChangeRequestDto.ListResponse.builder()
                .id(c.getId())
                .changeNumber(c.getChangeNumber())
                .title(c.getTitle())
                .changeType(c.getChangeType())
                .priority(c.getPriority())
                .riskLevel(c.getRiskLevel())
                .status(c.getStatus())
                .requesterName(c.getRequester().getFirstName() + " " + c.getRequester().getLastName())
                .assigneeName(c.getAssignee() != null ? c.getAssignee().getFirstName() + " " + c.getAssignee().getLastName() : null)
                .scheduledStart(c.getScheduledStart())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private ChangeRequestDto.ApprovalResponse toApprovalResponse(ChangeApproval a) {
        return ChangeRequestDto.ApprovalResponse.builder()
                .id(a.getId())
                .approverId(a.getApprover().getId())
                .approverName(a.getApprover().getFirstName() + " " + a.getApprover().getLastName())
                .approvalLevel(a.getApprovalLevel())
                .status(a.getStatus())
                .decisionDate(a.getDecisionDate())
                .comments(a.getComments())
                .build();
    }

    private ChangeRequestDto.CalendarEvent toCalendarEvent(ChangeRequest c) {
        String color = switch (c.getChangeType()) {
            case EMERGENCY -> "#ef4444";
            case NORMAL -> "#3b82f6";
            case STANDARD -> "#22c55e";
        };
        return ChangeRequestDto.CalendarEvent.builder()
                .id(c.getId())
                .changeNumber(c.getChangeNumber())
                .title(c.getTitle())
                .type(c.getChangeType())
                .status(c.getStatus())
                .start(c.getScheduledStart())
                .end(c.getScheduledEnd())
                .color(color)
                .build();
    }

    private ChangeRequestDto.UserSummary toUserSummary(User u) {
        return ChangeRequestDto.UserSummary.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFirstName() + " " + u.getLastName())
                .build();
    }

    private ChangeRequestDto.TeamSummary toTeamSummary(Team t) {
        return ChangeRequestDto.TeamSummary.builder()
                .id(t.getId())
                .name(t.getName())
                .build();
    }
}
