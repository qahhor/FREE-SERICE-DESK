package com.servicedesk.monolith.ticket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.monolith.ticket.dto.ProblemDto;
import com.servicedesk.monolith.ticket.entity.*;
import com.servicedesk.monolith.ticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final KnownErrorRepository knownErrorRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final AssetRepository assetRepository;
    private final ObjectMapper objectMapper;

    private static final AtomicInteger problemCounter = new AtomicInteger(1000);
    private static final AtomicInteger knownErrorCounter = new AtomicInteger(100);

    @Transactional
    public Problem createProblem(ProblemDto.CreateRequest request, User reporter) {
        String problemNumber = generateProblemNumber();

        Problem problem = Problem.builder()
                .problemNumber(problemNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority() != null ? request.getPriority() : Ticket.TicketPriority.MEDIUM)
                .impact(request.getImpact())
                .urgency(request.getUrgency())
                .status(Problem.ProblemStatus.IDENTIFIED)
                .reportedBy(reporter)
                .build();

        if (request.getAssigneeId() != null) {
            userRepository.findById(UUID.fromString(request.getAssigneeId())).ifPresent(problem::setAssignee);
        }

        // Link incidents
        if (request.getLinkedIncidentIds() != null) {
            for (String ticketId : request.getLinkedIncidentIds()) {
                ticketRepository.findById(UUID.fromString(ticketId)).ifPresent(ticket -> {
                    problem.getLinkedIncidents().add(ticket);
                    problem.setIncidentCount(problem.getIncidentCount() + 1);
                });
            }
        }

        // Link assets
        if (request.getAffectedAssetIds() != null) {
            for (String assetId : request.getAffectedAssetIds()) {
                assetRepository.findById(UUID.fromString(assetId))
                        .ifPresent(asset -> problem.getAffectedAssets().add(asset));
            }
        }

        Problem saved = problemRepository.save(problem);
        addHistory(saved, "CREATED", null, null, null, "Problem created");

        log.info("Created problem: {}", problemNumber);
        return saved;
    }

    @Transactional
    public Problem updateProblem(UUID id, ProblemDto.UpdateRequest request) {
        return problemRepository.findByIdAndDeletedFalse(id)
                .map(problem -> {
                    if (request.getTitle() != null) problem.setTitle(request.getTitle());
                    if (request.getDescription() != null) problem.setDescription(request.getDescription());
                    if (request.getCategory() != null) problem.setCategory(request.getCategory());
                    if (request.getPriority() != null) problem.setPriority(request.getPriority());
                    if (request.getImpact() != null) problem.setImpact(request.getImpact());
                    if (request.getUrgency() != null) problem.setUrgency(request.getUrgency());
                    if (request.getEstimatedImpactCost() != null) problem.setEstimatedImpactCost(request.getEstimatedImpactCost());

                    if (request.getAssigneeId() != null) {
                        userRepository.findById(UUID.fromString(request.getAssigneeId())).ifPresent(problem::setAssignee);
                    }

                    addHistory(problem, "UPDATED", null, null, null, "Problem updated");
                    return problemRepository.save(problem);
                })
                .orElseThrow(() -> new RuntimeException("Problem not found: " + id));
    }

    @Transactional
    public Problem updateStatus(UUID id, Problem.ProblemStatus newStatus, String notes) {
        return problemRepository.findByIdAndDeletedFalse(id)
                .map(problem -> {
                    Problem.ProblemStatus oldStatus = problem.getStatus();
                    problem.setStatus(newStatus);
                    addHistory(problem, "STATUS_CHANGED", "status", oldStatus.name(), newStatus.name(), notes);
                    return problemRepository.save(problem);
                })
                .orElseThrow(() -> new RuntimeException("Problem not found: " + id));
    }

    @Transactional
    public Problem addWorkaround(UUID id, String workaround) {
        return problemRepository.findByIdAndDeletedFalse(id)
                .map(problem -> {
                    problem.setWorkaround(workaround);
                    problem.setWorkaroundAvailable(true);
                    if (problem.getStatus() == Problem.ProblemStatus.DIAGNOSED) {
                        problem.setStatus(Problem.ProblemStatus.WORKAROUND_AVAILABLE);
                    }
                    addHistory(problem, "WORKAROUND_ADDED", null, null, null, "Workaround documented");
                    return problemRepository.save(problem);
                })
                .orElseThrow(() -> new RuntimeException("Problem not found: " + id));
    }

    @Transactional
    public Problem documentRootCause(UUID id, String rootCause, Problem.RootCauseCategory category, User analyst) {
        return problemRepository.findByIdAndDeletedFalse(id)
                .map(problem -> {
                    problem.setRootCause(rootCause);
                    problem.setRootCauseCategory(category);
                    problem.setRcaCompletedAt(LocalDateTime.now());
                    problem.setRcaCompletedBy(analyst);
                    problem.setStatus(Problem.ProblemStatus.ROOT_CAUSE_IDENTIFIED);
                    addHistory(problem, "ROOT_CAUSE_IDENTIFIED", null, null, category.name(),
                            "Root cause identified: " + category.name());
                    return problemRepository.save(problem);
                })
                .orElseThrow(() -> new RuntimeException("Problem not found: " + id));
    }

    @Transactional
    public Problem documentSolution(UUID id, String solution) {
        return problemRepository.findByIdAndDeletedFalse(id)
                .map(problem -> {
                    problem.setSolution(solution);
                    problem.setStatus(Problem.ProblemStatus.SOLUTION_FOUND);
                    addHistory(problem, "SOLUTION_DOCUMENTED", null, null, null, "Solution documented");
                    return problemRepository.save(problem);
                })
                .orElseThrow(() -> new RuntimeException("Problem not found: " + id));
    }

    @Transactional
    public Problem verifySolution(UUID id, User verifier) {
        return problemRepository.findByIdAndDeletedFalse(id)
                .map(problem -> {
                    problem.setSolutionVerified(true);
                    problem.setSolutionVerifiedAt(LocalDateTime.now());
                    problem.setSolutionVerifiedBy(verifier);
                    problem.setStatus(Problem.ProblemStatus.VERIFIED);
                    addHistory(problem, "SOLUTION_VERIFIED", null, null, null,
                            "Solution verified by " + verifier.getEmail());
                    return problemRepository.save(problem);
                })
                .orElseThrow(() -> new RuntimeException("Problem not found: " + id));
    }

    @Transactional
    public Problem linkIncident(UUID problemId, UUID ticketId) {
        Problem problem = problemRepository.findByIdAndDeletedFalse(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        problem.getLinkedIncidents().add(ticket);
        problem.setIncidentCount(problem.getLinkedIncidents().size());

        addHistory(problem, "INCIDENT_LINKED", null, null, ticket.getTicketNumber(),
                "Linked incident: " + ticket.getTicketNumber());

        return problemRepository.save(problem);
    }

    // Known Error Management
    @Transactional
    public KnownError createKnownError(UUID problemId) {
        Problem problem = problemRepository.findByIdAndDeletedFalse(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        if (problem.getRootCause() == null) {
            throw new RuntimeException("Root cause must be documented before creating known error");
        }

        String errorNumber = generateKnownErrorNumber();

        KnownError knownError = KnownError.builder()
                .errorNumber(errorNumber)
                .title(problem.getTitle())
                .description(problem.getDescription())
                .problem(problem)
                .rootCause(problem.getRootCause())
                .workaround(problem.getWorkaround())
                .permanentFix(problem.getSolution())
                .status(KnownError.ErrorStatus.ACTIVE)
                .fixAvailable(problem.getSolution() != null)
                .category(problem.getCategory())
                .priority(problem.getPriority())
                .incidentCount(problem.getIncidentCount())
                .build();

        KnownError saved = knownErrorRepository.save(knownError);
        problem.setKnownError(saved);
        problemRepository.save(problem);

        log.info("Created known error {} from problem {}", errorNumber, problem.getProblemNumber());
        return saved;
    }

    @Transactional
    public KnownError createKnownError(ProblemDto.KnownErrorCreateRequest request) {
        String errorNumber = generateKnownErrorNumber();

        KnownError knownError = KnownError.builder()
                .errorNumber(errorNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .symptoms(request.getSymptoms())
                .rootCause(request.getRootCause())
                .workaround(request.getWorkaround())
                .permanentFix(request.getPermanentFix())
                .affectedServices(request.getAffectedServices())
                .affectedSystems(request.getAffectedSystems())
                .status(KnownError.ErrorStatus.ACTIVE)
                .fixAvailable(request.getPermanentFix() != null)
                .category(request.getCategory())
                .priority(request.getPriority())
                .build();

        return knownErrorRepository.save(knownError);
    }

    @Transactional
    public KnownError markFixImplemented(UUID id) {
        return knownErrorRepository.findByIdAndDeletedFalse(id)
                .map(ke -> {
                    ke.setFixImplemented(true);
                    ke.setFixImplementedAt(LocalDateTime.now());
                    ke.setStatus(KnownError.ErrorStatus.RESOLVED);
                    return knownErrorRepository.save(ke);
                })
                .orElseThrow(() -> new RuntimeException("Known error not found: " + id));
    }

    public Page<KnownError> searchKnownErrors(String query, Pageable pageable) {
        return knownErrorRepository.search(query, pageable);
    }

    public Page<KnownError> getActiveKnownErrors(Pageable pageable) {
        return knownErrorRepository.findByStatus(KnownError.ErrorStatus.ACTIVE, pageable);
    }

    // Query methods
    public Optional<Problem> getProblem(UUID id) {
        return problemRepository.findByIdAndDeletedFalse(id);
    }

    public Page<Problem> getAllProblems(Pageable pageable) {
        return problemRepository.findAllActive(pageable);
    }

    public Page<Problem> getProblemsByStatus(Problem.ProblemStatus status, Pageable pageable) {
        return problemRepository.findByStatus(status, pageable);
    }

    public Page<Problem> getProblemsWithWorkaround(Pageable pageable) {
        return problemRepository.findWithWorkaround(pageable);
    }

    public List<Problem> getHighImpactProblems(int incidentThreshold) {
        return problemRepository.findHighImpactProblems(incidentThreshold);
    }

    public ProblemDto.ProblemMetrics getMetrics() {
        Map<String, Long> byCategory = new HashMap<>();
        // In production, aggregate by category with a proper query

        return ProblemDto.ProblemMetrics.builder()
                .totalProblems(problemRepository.count())
                .openProblems(problemRepository.countByStatus(Problem.ProblemStatus.IDENTIFIED) +
                             problemRepository.countByStatus(Problem.ProblemStatus.LOGGED) +
                             problemRepository.countByStatus(Problem.ProblemStatus.DIAGNOSED))
                .withWorkaround(problemRepository.countByStatus(Problem.ProblemStatus.WORKAROUND_AVAILABLE))
                .withRootCause(problemRepository.countByStatus(Problem.ProblemStatus.ROOT_CAUSE_IDENTIFIED))
                .resolved(problemRepository.countByStatus(Problem.ProblemStatus.CLOSED) +
                         problemRepository.countByStatus(Problem.ProblemStatus.VERIFIED))
                .knownErrors(knownErrorRepository.count())
                .activeKnownErrors(knownErrorRepository.countByStatus(KnownError.ErrorStatus.ACTIVE))
                .build();
    }

    private String generateProblemNumber() {
        Integer max = problemRepository.findMaxProblemNumber();
        int next = (max != null ? max : problemCounter.get()) + 1;
        problemCounter.set(next);
        return String.format("PRB%06d", next);
    }

    private String generateKnownErrorNumber() {
        Integer max = knownErrorRepository.findMaxErrorNumber();
        int next = (max != null ? max : knownErrorCounter.get()) + 1;
        knownErrorCounter.set(next);
        return String.format("KE%05d", next);
    }

    private void addHistory(Problem problem, String action, String field, String oldVal, String newVal, String desc) {
        ProblemHistory history = ProblemHistory.builder()
                .action(action)
                .fieldName(field)
                .oldValue(oldVal)
                .newValue(newVal)
                .description(desc)
                .build();
        problem.addHistoryEntry(history);
    }
}
