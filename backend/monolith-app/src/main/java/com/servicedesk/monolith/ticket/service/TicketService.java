package com.servicedesk.monolith.ticket.service;

import com.servicedesk.common.dto.PageResponse;
import com.servicedesk.common.exception.BadRequestException;
import com.servicedesk.common.exception.ForbiddenException;
import com.servicedesk.common.exception.ResourceNotFoundException;
import com.servicedesk.common.util.SecurityUtils;
import com.servicedesk.monolith.ticket.dto.*;
import com.servicedesk.monolith.ticket.entity.*;
import com.servicedesk.monolith.ticket.mapper.TicketCommentMapper;
import com.servicedesk.monolith.ticket.mapper.TicketMapper;
import com.servicedesk.monolith.ticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;
    private final TeamRepository teamRepository;
    private final TicketMapper ticketMapper;
    private final TicketCommentMapper commentMapper;

    @Transactional
    public TicketDto createTicket(CreateTicketRequest request) {
        User requester = SecurityUtils.getCurrentUserId()
                .flatMap(userRepository::findById)
                .orElseThrow(() -> new ForbiddenException("User not found"));

        Project project = projectRepository.findByIdAndDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Ticket ticket = Ticket.builder()
                .ticketNumber(generateTicketNumber(project.getKey()))
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(Ticket.TicketStatus.OPEN)
                .priority(request.getPriority() != null ? request.getPriority() : Ticket.TicketPriority.MEDIUM)
                .type(request.getType() != null ? request.getType() : Ticket.TicketType.QUESTION)
                .channel(request.getChannel() != null ? request.getChannel() : Ticket.TicketChannel.WEB)
                .project(project)
                .requester(requester)
                .dueDate(request.getDueDate())
                .externalId(request.getExternalId())
                .metadata(request.getMetadata())
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            ticket.setCategory(category);

            // Apply default assignee/team from category
            if (request.getAssigneeId() == null && category.getDefaultAssignee() != null) {
                ticket.setAssignee(category.getDefaultAssignee());
            }
            if (request.getTeamId() == null && category.getDefaultTeam() != null) {
                ticket.setTeam(category.getDefaultTeam());
            }
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            ticket.setAssignee(assignee);
        }

        if (request.getTeamId() != null) {
            Team team = teamRepository.findByIdAndDeletedFalse(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getTeamId()));
            ticket.setTeam(team);
        }

        if (request.getTags() != null) {
            ticket.setTags(request.getTags());
        }

        // Add history entry
        TicketHistory history = TicketHistory.builder()
                .user(requester)
                .action(TicketHistory.HistoryAction.CREATED)
                .description("Ticket created")
                .build();
        ticket.addHistoryEntry(history);

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Created ticket: {} by user: {}", savedTicket.getTicketNumber(), requester.getEmail());

        return ticketMapper.toDto(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketDto getTicket(UUID id) {
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return ticketMapper.toDto(ticket);
    }

    @Transactional(readOnly = true)
    public TicketDto getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumberAndDeletedFalse(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketNumber", ticketNumber));
        return ticketMapper.toDto(ticket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketDto> findTickets(TicketFilterRequest filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy()
        );
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Ticket> spec = buildSpecification(filter);
        Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);

        return PageResponse.from(tickets, ticketMapper.toDtoList(tickets.getContent()));
    }

    @Transactional
    public TicketDto updateTicket(UUID id, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User currentUser = SecurityUtils.getCurrentUserId()
                .flatMap(userRepository::findById)
                .orElseThrow(() -> new ForbiddenException("User not found"));

        if (request.getSubject() != null && !request.getSubject().equals(ticket.getSubject())) {
            addHistoryEntry(ticket, currentUser, TicketHistory.HistoryAction.UPDATED,
                    "subject", ticket.getSubject(), request.getSubject());
            ticket.setSubject(request.getSubject());
        }

        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }

        if (request.getStatus() != null && request.getStatus() != ticket.getStatus()) {
            addHistoryEntry(ticket, currentUser, TicketHistory.HistoryAction.STATUS_CHANGED,
                    "status", ticket.getStatus().name(), request.getStatus().name());

            Ticket.TicketStatus oldStatus = ticket.getStatus();
            ticket.setStatus(request.getStatus());

            // Track resolution time
            if (request.getStatus() == Ticket.TicketStatus.RESOLVED && ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(LocalDateTime.now());
            } else if (request.getStatus() == Ticket.TicketStatus.CLOSED && ticket.getClosedAt() == null) {
                ticket.setClosedAt(LocalDateTime.now());
            }

            // Track reopens
            if (oldStatus == Ticket.TicketStatus.CLOSED || oldStatus == Ticket.TicketStatus.RESOLVED) {
                if (request.getStatus() == Ticket.TicketStatus.OPEN || request.getStatus() == Ticket.TicketStatus.IN_PROGRESS) {
                    ticket.setReopenedCount(ticket.getReopenedCount() + 1);
                    ticket.setResolvedAt(null);
                    ticket.setClosedAt(null);
                }
            }
        }

        if (request.getPriority() != null && request.getPriority() != ticket.getPriority()) {
            addHistoryEntry(ticket, currentUser, TicketHistory.HistoryAction.PRIORITY_CHANGED,
                    "priority", ticket.getPriority().name(), request.getPriority().name());
            ticket.setPriority(request.getPriority());
        }

        if (request.getType() != null) {
            ticket.setType(request.getType());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            if (ticket.getCategory() == null || !ticket.getCategory().getId().equals(request.getCategoryId())) {
                addHistoryEntry(ticket, currentUser, TicketHistory.HistoryAction.CATEGORY_CHANGED,
                        "category",
                        ticket.getCategory() != null ? ticket.getCategory().getName() : null,
                        category.getName());
            }
            ticket.setCategory(category);
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            if (ticket.getAssignee() == null || !ticket.getAssignee().getId().equals(request.getAssigneeId())) {
                addHistoryEntry(ticket, currentUser, TicketHistory.HistoryAction.ASSIGNED,
                        "assignee",
                        ticket.getAssignee() != null ? ticket.getAssignee().getFullName() : null,
                        assignee.getFullName());
            }
            ticket.setAssignee(assignee);
        }

        if (request.getTeamId() != null) {
            Team team = teamRepository.findByIdAndDeletedFalse(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getTeamId()));
            if (ticket.getTeam() == null || !ticket.getTeam().getId().equals(request.getTeamId())) {
                addHistoryEntry(ticket, currentUser, TicketHistory.HistoryAction.TEAM_CHANGED,
                        "team",
                        ticket.getTeam() != null ? ticket.getTeam().getName() : null,
                        team.getName());
            }
            ticket.setTeam(team);
        }

        if (request.getDueDate() != null) {
            ticket.setDueDate(request.getDueDate());
        }

        if (request.getTags() != null) {
            ticket.setTags(request.getTags());
        }

        if (request.getMetadata() != null) {
            ticket.setMetadata(request.getMetadata());
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Updated ticket: {}", savedTicket.getTicketNumber());

        return ticketMapper.toDto(savedTicket);
    }

    @Transactional
    public TicketDto assignTicket(UUID ticketId, UUID assigneeId) {
        UpdateTicketRequest request = UpdateTicketRequest.builder()
                .assigneeId(assigneeId)
                .status(Ticket.TicketStatus.IN_PROGRESS)
                .build();
        return updateTicket(ticketId, request);
    }

    @Transactional
    public TicketDto closeTicket(UUID ticketId) {
        UpdateTicketRequest request = UpdateTicketRequest.builder()
                .status(Ticket.TicketStatus.CLOSED)
                .build();
        return updateTicket(ticketId, request);
    }

    @Transactional
    public void deleteTicket(UUID id) {
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        ticket.setDeleted(true);
        ticketRepository.save(ticket);
        log.info("Deleted ticket: {}", ticket.getTicketNumber());
    }

    @Transactional
    public TicketCommentDto addComment(UUID ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User author = SecurityUtils.getCurrentUserId()
                .flatMap(userRepository::findById)
                .orElseThrow(() -> new ForbiddenException("User not found"));

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .author(author)
                .content(request.getContent())
                .contentHtml(request.getContentHtml())
                .type(request.getType())
                .build();

        // Track first response time
        if (ticket.getFirstResponseAt() == null &&
            author.getRole() != User.UserRole.CUSTOMER &&
            request.getType() == TicketComment.CommentType.PUBLIC) {
            ticket.setFirstResponseAt(LocalDateTime.now());
        }

        ticket.addComment(comment);

        addHistoryEntry(ticket, author, TicketHistory.HistoryAction.COMMENT_ADDED,
                null, null, request.getType().name() + " comment added");

        ticketRepository.save(ticket);
        log.info("Added comment to ticket: {} by user: {}", ticket.getTicketNumber(), author.getEmail());

        return commentMapper.toDto(comment);
    }

    @Transactional(readOnly = true)
    public List<TicketCommentDto> getComments(UUID ticketId) {
        List<TicketComment> comments = commentRepository.findByTicketId(ticketId);
        return commentMapper.toDtoList(comments);
    }

    @Transactional
    public TicketDto addWatcher(UUID ticketId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ticket.getWatchers().add(user);

        User currentUser = SecurityUtils.getCurrentUserId()
                .flatMap(userRepository::findById)
                .orElse(null);

        addHistoryEntry(ticket, currentUser, TicketHistory.HistoryAction.WATCHER_ADDED,
                null, null, user.getFullName());

        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto removeWatcher(UUID ticketId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        ticket.getWatchers().removeIf(w -> w.getId().equals(userId));

        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    private String generateTicketNumber(String projectKey) {
        Integer maxNumber = ticketRepository.findMaxTicketNumberByPrefix(projectKey + "-");
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return projectKey + "-" + nextNumber;
    }

    private void addHistoryEntry(Ticket ticket, User user, TicketHistory.HistoryAction action,
                                 String fieldName, String oldValue, String newValue) {
        TicketHistory history = TicketHistory.builder()
                .user(user)
                .action(action)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
        ticket.addHistoryEntry(history);
    }

    private Specification<Ticket> buildSpecification(TicketFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                String search = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("subject")), search),
                        cb.like(cb.lower(root.get("ticketNumber")), search),
                        cb.like(cb.lower(root.get("description")), search)
                ));
            }

            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            if (filter.getPriorities() != null && !filter.getPriorities().isEmpty()) {
                predicates.add(root.get("priority").in(filter.getPriorities()));
            }

            if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
                predicates.add(root.get("type").in(filter.getTypes()));
            }

            if (filter.getChannels() != null && !filter.getChannels().isEmpty()) {
                predicates.add(root.get("channel").in(filter.getChannels()));
            }

            if (filter.getProjectIds() != null && !filter.getProjectIds().isEmpty()) {
                predicates.add(root.get("project").get("id").in(filter.getProjectIds()));
            }

            if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                predicates.add(root.get("category").get("id").in(filter.getCategoryIds()));
            }

            if (filter.getAssigneeIds() != null && !filter.getAssigneeIds().isEmpty()) {
                predicates.add(root.get("assignee").get("id").in(filter.getAssigneeIds()));
            }

            if (filter.getRequesterIds() != null && !filter.getRequesterIds().isEmpty()) {
                predicates.add(root.get("requester").get("id").in(filter.getRequesterIds()));
            }

            if (filter.getTeamIds() != null && !filter.getTeamIds().isEmpty()) {
                predicates.add(root.get("team").get("id").in(filter.getTeamIds()));
            }

            if (filter.getUnassigned() != null && filter.getUnassigned()) {
                predicates.add(cb.isNull(root.get("assignee")));
            }

            if (filter.getOverdue() != null && filter.getOverdue()) {
                predicates.add(cb.lessThan(root.get("dueDate"), LocalDateTime.now()));
                predicates.add(root.get("status").in(
                        Ticket.TicketStatus.OPEN,
                        Ticket.TicketStatus.IN_PROGRESS,
                        Ticket.TicketStatus.PENDING
                ));
            }

            if (filter.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
            }

            if (filter.getCreatedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
            }

            if (filter.getDueDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
            }

            if (filter.getDueDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
