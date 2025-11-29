package com.servicedesk.monolith.ticket.controller;

import com.servicedesk.common.dto.ApiResponse;
import com.servicedesk.common.dto.PageResponse;
import com.servicedesk.monolith.ticket.dto.*;
import com.servicedesk.monolith.ticket.entity.Ticket;
import com.servicedesk.monolith.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket management endpoints")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "Create Ticket", description = "Create a new support ticket")
    public ResponseEntity<ApiResponse<TicketDto>> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketDto ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ticket, "Ticket created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Ticket", description = "Get ticket by ID")
    public ResponseEntity<ApiResponse<TicketDto>> getTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID id) {
        TicketDto ticket = ticketService.getTicket(id);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @GetMapping("/number/{ticketNumber}")
    @Operation(summary = "Get Ticket by Number", description = "Get ticket by ticket number")
    public ResponseEntity<ApiResponse<TicketDto>> getTicketByNumber(
            @Parameter(description = "Ticket number (e.g., PROJ-123)") @PathVariable String ticketNumber) {
        TicketDto ticket = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @GetMapping
    @Operation(summary = "List Tickets", description = "Get paginated list of tickets with filters")
    public ResponseEntity<ApiResponse<PageResponse<TicketDto>>> listTickets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Set<Ticket.TicketStatus> statuses,
            @RequestParam(required = false) Set<Ticket.TicketPriority> priorities,
            @RequestParam(required = false) Set<Ticket.TicketType> types,
            @RequestParam(required = false) Set<Ticket.TicketChannel> channels,
            @RequestParam(required = false) Set<UUID> projectIds,
            @RequestParam(required = false) Set<UUID> categoryIds,
            @RequestParam(required = false) Set<UUID> assigneeIds,
            @RequestParam(required = false) Set<UUID> requesterIds,
            @RequestParam(required = false) Set<UUID> teamIds,
            @RequestParam(required = false) Set<String> tags,
            @RequestParam(required = false) LocalDateTime createdFrom,
            @RequestParam(required = false) LocalDateTime createdTo,
            @RequestParam(required = false) LocalDateTime dueDateFrom,
            @RequestParam(required = false) LocalDateTime dueDateTo,
            @RequestParam(required = false) Boolean unassigned,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        TicketFilterRequest filter = TicketFilterRequest.builder()
                .search(search)
                .statuses(statuses)
                .priorities(priorities)
                .types(types)
                .channels(channels)
                .projectIds(projectIds)
                .categoryIds(categoryIds)
                .assigneeIds(assigneeIds)
                .requesterIds(requesterIds)
                .teamIds(teamIds)
                .tags(tags)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .dueDateFrom(dueDateFrom)
                .dueDateTo(dueDateTo)
                .unassigned(unassigned)
                .overdue(overdue)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        PageResponse<TicketDto> tickets = ticketService.findTickets(filter);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update Ticket", description = "Update an existing ticket")
    public ResponseEntity<ApiResponse<TicketDto>> updateTicket(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request) {
        TicketDto ticket = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete Ticket", description = "Soft delete a ticket (Admin/Manager only)")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Ticket deleted successfully"));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    @Operation(summary = "Assign Ticket", description = "Assign ticket to an agent")
    public ResponseEntity<ApiResponse<TicketDto>> assignTicket(
            @PathVariable UUID id,
            @RequestParam UUID assigneeId) {
        TicketDto ticket = ticketService.assignTicket(id, assigneeId);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket assigned successfully"));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close Ticket", description = "Close a ticket")
    public ResponseEntity<ApiResponse<TicketDto>> closeTicket(@PathVariable UUID id) {
        TicketDto ticket = ticketService.closeTicket(id);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket closed successfully"));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add Comment", description = "Add a comment to a ticket")
    public ResponseEntity<ApiResponse<TicketCommentDto>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCommentRequest request) {
        TicketCommentDto comment = ticketService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(comment, "Comment added successfully"));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get Comments", description = "Get all comments for a ticket")
    public ResponseEntity<ApiResponse<List<TicketCommentDto>>> getComments(@PathVariable UUID id) {
        List<TicketCommentDto> comments = ticketService.getComments(id);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @PostMapping("/{id}/watchers")
    @Operation(summary = "Add Watcher", description = "Add a watcher to a ticket")
    public ResponseEntity<ApiResponse<TicketDto>> addWatcher(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        TicketDto ticket = ticketService.addWatcher(id, userId);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Watcher added successfully"));
    }

    @DeleteMapping("/{id}/watchers/{userId}")
    @Operation(summary = "Remove Watcher", description = "Remove a watcher from a ticket")
    public ResponseEntity<ApiResponse<TicketDto>> removeWatcher(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        TicketDto ticket = ticketService.removeWatcher(id, userId);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Watcher removed successfully"));
    }
}
