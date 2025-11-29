package com.servicedesk.monolith.ticket.repository;

import com.servicedesk.monolith.ticket.entity.KnownError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnownErrorRepository extends JpaRepository<KnownError, UUID> {

    Optional<KnownError> findByErrorNumber(String errorNumber);

    Optional<KnownError> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT k FROM KnownError k WHERE k.deleted = false")
    Page<KnownError> findAllActive(Pageable pageable);

    @Query("SELECT k FROM KnownError k WHERE k.deleted = false AND k.status = :status")
    Page<KnownError> findByStatus(@Param("status") KnownError.ErrorStatus status, Pageable pageable);

    @Query("SELECT k FROM KnownError k WHERE k.deleted = false " +
           "AND (LOWER(k.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(k.symptoms) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(k.workaround) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<KnownError> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT k FROM KnownError k WHERE k.deleted = false AND k.fixAvailable = true AND k.fixImplemented = false")
    Page<KnownError> findWithPendingFix(Pageable pageable);

    @Query("SELECT COUNT(k) FROM KnownError k WHERE k.deleted = false AND k.status = :status")
    long countByStatus(@Param("status") KnownError.ErrorStatus status);

    @Query("SELECT MAX(CAST(SUBSTRING(k.errorNumber, 3) AS integer)) FROM KnownError k " +
           "WHERE k.errorNumber LIKE 'KE%'")
    Integer findMaxErrorNumber();
}
