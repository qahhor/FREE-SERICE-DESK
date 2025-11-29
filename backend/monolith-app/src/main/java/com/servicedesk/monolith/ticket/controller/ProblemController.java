package com.servicedesk.monolith.ticket.controller;

import com.servicedesk.monolith.ticket.dto.ProblemDto;
import com.servicedesk.monolith.ticket.entity.*;
import com.servicedesk.monolith.ticket.service.ProblemService;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<ProblemDto.ListResponse>> getAllProblems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Problem.ProblemStatus status) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Page<Problem> problems = status != null
                ? problemService.getProblemsByStatus(status, PageRequest.of(page, size, sort))
                : problemService.getAllProblems(PageRequest.of(page, size, sort));

        return ResponseEntity.ok(problems.map(this::toListResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.Response> getProblem(@PathVariable UUID id) {
        return problemService.getProblem(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.Response> createProblem(
            @Valid @RequestBody ProblemDto.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        Problem problem = problemService.createProblem(request, currentUser);
        return ResponseEntity.ok(toResponse(problem));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.Response> updateProblem(
            @PathVariable UUID id,
            @Valid @RequestBody ProblemDto.UpdateRequest request) {
        Problem problem = problemService.updateProblem(id, request);
        return ResponseEntity.ok(toResponse(problem));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.Response> updateStatus(
            @PathVariable UUID id,
            @RequestBody ProblemDto.StatusUpdate statusUpdate) {
        Problem problem = problemService.updateStatus(id, statusUpdate.getStatus(), statusUpdate.getNotes());
        return ResponseEntity.ok(toResponse(problem));
    }

    @PostMapping("/{id}/workaround")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.Response> addWorkaround(
            @PathVariable UUID id,
            @RequestBody ProblemDto.WorkaroundUpdate workaround) {
        Problem problem = problemService.addWorkaround(id, workaround.getWorkaround());
        return ResponseEntity.ok(toResponse(problem));
    }

    @PostMapping("/{id}/root-cause")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.Response> documentRootCause(
            @PathVariable UUID id,
            @RequestParam String rootCause,
            @RequestParam Problem.RootCauseCategory category,
            @AuthenticationPrincipal User currentUser) {
        Problem problem = problemService.documentRootCause(id, rootCause, category, currentUser);
        return ResponseEntity.ok(toResponse(problem));
    }

    @PostMapping("/{id}/solution")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.Response> documentSolution(
            @PathVariable UUID id,
            @RequestBody ProblemDto.SolutionUpdate solution) {
        Problem problem = problemService.documentSolution(id, solution.getSolution());
        return ResponseEntity.ok(toResponse(problem));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.Response> verifySolution(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        Problem problem = problemService.verifySolution(id, currentUser);
        return ResponseEntity.ok(toResponse(problem));
    }

    @PostMapping("/{problemId}/incidents/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.Response> linkIncident(
            @PathVariable UUID problemId,
            @PathVariable UUID ticketId) {
        Problem problem = problemService.linkIncident(problemId, ticketId);
        return ResponseEntity.ok(toResponse(problem));
    }

    @PostMapping("/{id}/known-error")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.KnownErrorResponse> createKnownError(@PathVariable UUID id) {
        KnownError ke = problemService.createKnownError(id);
        return ResponseEntity.ok(toKnownErrorResponse(ke));
    }

    @GetMapping("/with-workaround")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<ProblemDto.ListResponse>> getProblemsWithWorkaround(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Problem> problems = problemService.getProblemsWithWorkaround(PageRequest.of(page, size));
        return ResponseEntity.ok(problems.map(this::toListResponse));
    }

    @GetMapping("/high-impact")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<ProblemDto.ListResponse>> getHighImpactProblems(
            @RequestParam(defaultValue = "3") int incidentThreshold) {
        List<Problem> problems = problemService.getHighImpactProblems(incidentThreshold);
        return ResponseEntity.ok(problems.stream().map(this::toListResponse).collect(Collectors.toList()));
    }

    // Known Errors endpoints
    @GetMapping("/known-errors")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<ProblemDto.KnownErrorResponse>> getKnownErrors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Page<KnownError> errors = search != null
                ? problemService.searchKnownErrors(search, PageRequest.of(page, size))
                : problemService.getActiveKnownErrors(PageRequest.of(page, size));

        return ResponseEntity.ok(errors.map(this::toKnownErrorResponse));
    }

    @PostMapping("/known-errors")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.KnownErrorResponse> createKnownError(
            @Valid @RequestBody ProblemDto.KnownErrorCreateRequest request) {
        KnownError ke = problemService.createKnownError(request);
        return ResponseEntity.ok(toKnownErrorResponse(ke));
    }

    @PostMapping("/known-errors/{id}/fix-implemented")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.KnownErrorResponse> markFixImplemented(@PathVariable UUID id) {
        KnownError ke = problemService.markFixImplemented(id);
        return ResponseEntity.ok(toKnownErrorResponse(ke));
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ProblemDto.ProblemMetrics> getMetrics() {
        return ResponseEntity.ok(problemService.getMetrics());
    }

    // Mappers
    private ProblemDto.Response toResponse(Problem p) {
        return ProblemDto.Response.builder()
                .id(p.getId())
                .problemNumber(p.getProblemNumber())
                .title(p.getTitle())
                .description(p.getDescription())
                .category(p.getCategory())
                .priority(p.getPriority())
                .impact(p.getImpact())
                .urgency(p.getUrgency())
                .status(p.getStatus())
                .reportedBy(toUserSummary(p.getReportedBy()))
                .assignee(p.getAssignee() != null ? toUserSummary(p.getAssignee()) : null)
                .team(p.getTeam() != null ? toTeamSummary(p.getTeam()) : null)
                .rootCause(p.getRootCause())
                .rootCauseCategory(p.getRootCauseCategory())
                .rcaCompletedAt(p.getRcaCompletedAt())
                .rcaCompletedBy(p.getRcaCompletedBy() != null ? toUserSummary(p.getRcaCompletedBy()) : null)
                .workaround(p.getWorkaround())
                .workaroundAvailable(p.getWorkaroundAvailable())
                .solution(p.getSolution())
                .solutionVerified(p.getSolutionVerified())
                .solutionVerifiedAt(p.getSolutionVerifiedAt())
                .solutionVerifiedBy(p.getSolutionVerifiedBy() != null ? toUserSummary(p.getSolutionVerifiedBy()) : null)
                .incidentCount(p.getIncidentCount())
                .estimatedImpactCost(p.getEstimatedImpactCost())
                .projectId(p.getProject() != null ? p.getProject().getId() : null)
                .knownError(p.getKnownError() != null ? toKnownErrorSummary(p.getKnownError()) : null)
                .linkedIncidentsCount(p.getLinkedIncidents().size())
                .affectedAssetsCount(p.getAffectedAssets().size())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private ProblemDto.ListResponse toListResponse(Problem p) {
        return ProblemDto.ListResponse.builder()
                .id(p.getId())
                .problemNumber(p.getProblemNumber())
                .title(p.getTitle())
                .category(p.getCategory())
                .priority(p.getPriority())
                .status(p.getStatus())
                .assigneeName(p.getAssignee() != null ? p.getAssignee().getFirstName() + " " + p.getAssignee().getLastName() : null)
                .incidentCount(p.getIncidentCount())
                .workaroundAvailable(p.getWorkaroundAvailable())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private ProblemDto.KnownErrorResponse toKnownErrorResponse(KnownError ke) {
        return ProblemDto.KnownErrorResponse.builder()
                .id(ke.getId())
                .errorNumber(ke.getErrorNumber())
                .title(ke.getTitle())
                .description(ke.getDescription())
                .problemId(ke.getProblem() != null ? ke.getProblem().getId() : null)
                .problemNumber(ke.getProblem() != null ? ke.getProblem().getProblemNumber() : null)
                .symptoms(ke.getSymptoms())
                .rootCause(ke.getRootCause())
                .workaround(ke.getWorkaround())
                .permanentFix(ke.getPermanentFix())
                .status(ke.getStatus())
                .fixAvailable(ke.getFixAvailable())
                .fixImplemented(ke.getFixImplemented())
                .fixImplementedAt(ke.getFixImplementedAt())
                .affectedServices(ke.getAffectedServices())
                .affectedSystems(ke.getAffectedSystems())
                .incidentCount(ke.getIncidentCount())
                .category(ke.getCategory())
                .priority(ke.getPriority())
                .createdAt(ke.getCreatedAt())
                .updatedAt(ke.getUpdatedAt())
                .build();
    }

    private ProblemDto.KnownErrorSummary toKnownErrorSummary(KnownError ke) {
        return ProblemDto.KnownErrorSummary.builder()
                .id(ke.getId())
                .errorNumber(ke.getErrorNumber())
                .title(ke.getTitle())
                .build();
    }

    private ProblemDto.UserSummary toUserSummary(User u) {
        return ProblemDto.UserSummary.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFirstName() + " " + u.getLastName())
                .build();
    }

    private ProblemDto.TeamSummary toTeamSummary(Team t) {
        return ProblemDto.TeamSummary.builder()
                .id(t.getId())
                .name(t.getName())
                .build();
    }
}
