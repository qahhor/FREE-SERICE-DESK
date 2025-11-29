package com.servicedesk.monolith.ticket.service;
\nimport org.springframework.context.ApplicationEventPublisher;

import com.servicedesk.monolith.ticket.dto.ChangeRequestDto;
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
public class ChangeService {

    private final ChangeRequestRepository changeRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final AssetRepository assetRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final AtomicInteger changeCounter = new AtomicInteger(1000);

    @Transactional
    public ChangeRequest createChange(ChangeRequestDto.CreateRequest request, User requester) {
        String changeNumber = generateChangeNumber();

        ChangeRequest change = ChangeRequest.builder()
                .changeNumber(changeNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .justification(request.getJustification())
                .changeType(request.getChangeType() != null ? request.getChangeType() : ChangeRequest.ChangeType.NORMAL)
                .category(request.getCategory())
                .priority(request.getPriority() != null ? request.getPriority() : Ticket.TicketPriority.MEDIUM)
                .riskLevel(request.getRiskLevel() != null ? request.getRiskLevel() : ChangeRequest.RiskLevel.MEDIUM)
                .impact(request.getImpact() != null ? request.getImpact() : ChangeRequest.ImpactLevel.MEDIUM)
                .status(ChangeRequest.ChangeStatus.DRAFT)
                .requester(requester)
                .scheduledStart(request.getScheduledStart())
                .scheduledEnd(request.getScheduledEnd())
                .implementationPlan(request.getImplementationPlan())
                .rollbackPlan(request.getRollbackPlan())
                .testPlan(request.getTestPlan())
                .communicationPlan(request.getCommunicationPlan())
                .build();

        if (request.getAssigneeId() != null) {
            userRepository.findById(UUID.fromString(request.getAssigneeId())).ifPresent(change::setAssignee);
        }

        ChangeRequest saved = changeRepository.save(change);
        addHistory(saved, "CREATED", null, null, null, "Change request created");

        log.info("Created change request: {}", changeNumber);
        return saved;
    }

    @Transactional
    public ChangeRequest updateChange(UUID id, ChangeRequestDto.UpdateRequest request) {
        return changeRepository.findByIdAndDeletedFalse(id)
                .map(change -> {
                    if (request.getTitle() != null) change.setTitle(request.getTitle());
                    if (request.getDescription() != null) change.setDescription(request.getDescription());
                    if (request.getJustification() != null) change.setJustification(request.getJustification());
                    if (request.getChangeType() != null) change.setChangeType(request.getChangeType());
                    if (request.getCategory() != null) change.setCategory(request.getCategory());
                    if (request.getPriority() != null) change.setPriority(request.getPriority());
                    if (request.getRiskLevel() != null) change.setRiskLevel(request.getRiskLevel());
                    if (request.getImpact() != null) change.setImpact(request.getImpact());
                    if (request.getScheduledStart() != null) change.setScheduledStart(request.getScheduledStart());
                    if (request.getScheduledEnd() != null) change.setScheduledEnd(request.getScheduledEnd());
                    if (request.getImplementationPlan() != null) change.setImplementationPlan(request.getImplementationPlan());
                    if (request.getRollbackPlan() != null) change.setRollbackPlan(request.getRollbackPlan());
                    if (request.getTestPlan() != null) change.setTestPlan(request.getTestPlan());
                    if (request.getCommunicationPlan() != null) change.setCommunicationPlan(request.getCommunicationPlan());

                    if (request.getAssigneeId() != null) {
                        userRepository.findById(UUID.fromString(request.getAssigneeId())).ifPresent(change::setAssignee);
                    }

                    addHistory(change, "UPDATED", null, null, null, "Change request updated");
                    return changeRepository.save(change);
                })
                .orElseThrow(() -> new RuntimeException("Change not found: " + id));
    }

    @Transactional
    public ChangeRequest submitForApproval(UUID id) {
        return changeRepository.findByIdAndDeletedFalse(id)
                .map(change -> {
                    if (change.getStatus() != ChangeRequest.ChangeStatus.DRAFT) {
                        throw new RuntimeException("Can only submit draft changes");
                    }
                    change.setStatus(ChangeRequest.ChangeStatus.SUBMITTED);
                    addHistory(change, "STATUS_CHANGED", "status", "DRAFT", "SUBMITTED", "Submitted for approval");

                    // Auto-approve standard changes
                    if (change.getChangeType() == ChangeRequest.ChangeType.STANDARD) {
                        change.setStatus(ChangeRequest.ChangeStatus.APPROVED);
                        addHistory(change, "STATUS_CHANGED", "status", "SUBMITTED", "APPROVED", "Auto-approved (standard change)");
                    } else {
                        change.setStatus(ChangeRequest.ChangeStatus.PENDING_APPROVAL);
                        addHistory(change, "STATUS_CHANGED", "status", "SUBMITTED", "PENDING_APPROVAL", "Awaiting approval");
                        sendApprovalNotification(change);
                    }

                    return changeRepository.save(change);
                })
                .orElseThrow(() -> new RuntimeException("Change not found: " + id));
    }

    @Transactional
    public ChangeApproval processApproval(UUID changeId, UUID approverId, ChangeRequestDto.ApprovalDecision decision) {
        ChangeRequest change = changeRepository.findByIdAndDeletedFalse(changeId)
                .orElseThrow(() -> new RuntimeException("Change not found: " + changeId));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found: " + approverId));

        ChangeApproval approval = change.getApprovals().stream()
                .filter(a -> a.getApprover().getId().equals(approverId.toString()) &&
                            a.getStatus() == ChangeApproval.ApprovalStatus.PENDING)
                .findFirst()
                .orElseGet(() -> {
                    ChangeApproval newApproval = ChangeApproval.builder()
                            .changeRequest(change)
                            .approver(approver)
                            .approvalLevel(1)
                            .build();
                    change.getApprovals().add(newApproval);
                    return newApproval;
                });

        approval.setStatus(decision.getDecision());
        approval.setDecisionDate(LocalDateTime.now());
        approval.setComments(decision.getComments());

        if (decision.getDecision() == ChangeApproval.ApprovalStatus.APPROVED) {
            change.setStatus(ChangeRequest.ChangeStatus.APPROVED);
            addHistory(change, "APPROVED", null, null, null, "Approved by " + approver.getEmail());
        } else if (decision.getDecision() == ChangeApproval.ApprovalStatus.REJECTED) {
            change.setStatus(ChangeRequest.ChangeStatus.REJECTED);
            addHistory(change, "REJECTED", null, null, null, "Rejected by " + approver.getEmail() + ": " + decision.getComments());
        }

        changeRepository.save(change);
        return approval;
    }

    @Transactional
    public ChangeRequest updateStatus(UUID id, ChangeRequest.ChangeStatus newStatus, String notes) {
        return changeRepository.findByIdAndDeletedFalse(id)
                .map(change -> {
                    ChangeRequest.ChangeStatus oldStatus = change.getStatus();
                    change.setStatus(newStatus);

                    if (newStatus == ChangeRequest.ChangeStatus.IN_PROGRESS) {
                        change.setActualStart(LocalDateTime.now());
                    } else if (newStatus == ChangeRequest.ChangeStatus.COMPLETED ||
                               newStatus == ChangeRequest.ChangeStatus.FAILED ||
                               newStatus == ChangeRequest.ChangeStatus.ROLLED_BACK) {
                        change.setActualEnd(LocalDateTime.now());
                    }

                    addHistory(change, "STATUS_CHANGED", "status", oldStatus.name(), newStatus.name(), notes);
                    return changeRepository.save(change);
                })
                .orElseThrow(() -> new RuntimeException("Change not found: " + id));
    }

    @Transactional
    public ChangeRequest completeReview(UUID id, ChangeRequestDto.ReviewRequest review) {
        return changeRepository.findByIdAndDeletedFalse(id)
                .map(change -> {
                    change.setSuccess(review.getSuccess());
                    change.setReviewNotes(review.getReviewNotes());
                    change.setStatus(ChangeRequest.ChangeStatus.CLOSED);
                    addHistory(change, "REVIEWED", null, null, null,
                            "PIR completed. Success: " + review.getSuccess());
                    return changeRepository.save(change);
                })
                .orElseThrow(() -> new RuntimeException("Change not found: " + id));
    }

    public Optional<ChangeRequest> getChange(UUID id) {
        return changeRepository.findByIdAndDeletedFalse(id);
    }

    public Page<ChangeRequest> getAllChanges(Pageable pageable) {
        return changeRepository.findAllActive(pageable);
    }

    public Page<ChangeRequest> getChangesByStatus(ChangeRequest.ChangeStatus status, Pageable pageable) {
        return changeRepository.findByStatus(status, pageable);
    }

    public Page<ChangeRequest> getPendingApprovals(Pageable pageable) {
        return changeRepository.findPendingApproval(pageable);
    }

    public List<ChangeRequest> getScheduledChanges(LocalDateTime start, LocalDateTime end) {
        return changeRepository.findScheduledInRange(start, end);
    }

    public ChangeRequestDto.ChangeMetrics getMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        return ChangeRequestDto.ChangeMetrics.builder()
                .totalChanges(changeRepository.count())
                .pendingApproval(changeRepository.countByStatus(ChangeRequest.ChangeStatus.PENDING_APPROVAL))
                .scheduled(changeRepository.countByStatus(ChangeRequest.ChangeStatus.SCHEDULED))
                .inProgress(changeRepository.countByStatus(ChangeRequest.ChangeStatus.IN_PROGRESS))
                .completedSuccessfully(changeRepository.countSuccessful(startDate, endDate))
                .failed(changeRepository.countFailed(startDate, endDate))
                .rolledBack(changeRepository.countByStatus(ChangeRequest.ChangeStatus.ROLLED_BACK))
                .build();
    }

    private String generateChangeNumber() {
        Integer max = changeRepository.findMaxChangeNumber();
        int next = (max != null ? max : changeCounter.get()) + 1;
        changeCounter.set(next);
        return String.format("CHG%06d", next);
    }

    private void addHistory(ChangeRequest change, String action, String field, String oldVal, String newVal, String desc) {
        ChangeHistory history = ChangeHistory.builder()
                .action(action)
                .fieldName(field)
                .oldValue(oldVal)
                .newValue(newVal)
                .description(desc)
                .build();
        change.addHistoryEntry(history);
    }

    private void sendApprovalNotification(ChangeRequest change) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "CHANGE_APPROVAL_REQUIRED");
        notification.put("changeId", change.getId());
        notification.put("changeNumber", change.getChangeNumber());
        notification.put("title", change.getTitle());
        notification.put("changeType", change.getChangeType().name());
        notification.put("requesterId", change.getRequester().getId());
        eventPublisher.convertAndSend("notification.queue", notification);
    }
}
