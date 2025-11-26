package com.servicedesk.ticket.repository;

import com.servicedesk.ticket.entity.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {

    Optional<TicketComment> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT c FROM TicketComment c WHERE c.ticket.id = :ticketId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<TicketComment> findByTicketId(@Param("ticketId") UUID ticketId);

    @Query("SELECT c FROM TicketComment c WHERE c.ticket.id = :ticketId AND c.deleted = false ORDER BY c.createdAt ASC")
    Page<TicketComment> findByTicketId(@Param("ticketId") UUID ticketId, Pageable pageable);

    @Query("SELECT c FROM TicketComment c WHERE c.ticket.id = :ticketId AND c.type = :type AND c.deleted = false ORDER BY c.createdAt ASC")
    List<TicketComment> findByTicketIdAndType(@Param("ticketId") UUID ticketId, @Param("type") TicketComment.CommentType type);

    @Query("SELECT COUNT(c) FROM TicketComment c WHERE c.ticket.id = :ticketId AND c.deleted = false")
    long countByTicketId(@Param("ticketId") UUID ticketId);

    @Query("SELECT c FROM TicketComment c WHERE c.author.id = :authorId AND c.deleted = false ORDER BY c.createdAt DESC")
    Page<TicketComment> findByAuthorId(@Param("authorId") UUID authorId, Pageable pageable);
}
