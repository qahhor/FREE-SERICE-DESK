package com.servicedesk.monolith.ticket.repository;

import com.servicedesk.monolith.ticket.entity.AssetMaintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, UUID> {

    @Query("SELECT m FROM AssetMaintenance m WHERE m.deleted = false AND m.asset.id = :assetId ORDER BY m.scheduledDate DESC")
    List<AssetMaintenance> findByAssetId(@Param("assetId") UUID assetId);

    @Query("SELECT m FROM AssetMaintenance m WHERE m.deleted = false AND m.status = :status")
    Page<AssetMaintenance> findByStatus(@Param("status") AssetMaintenance.MaintenanceStatus status, Pageable pageable);

    @Query("SELECT m FROM AssetMaintenance m WHERE m.deleted = false " +
           "AND m.scheduledDate BETWEEN :startDate AND :endDate")
    List<AssetMaintenance> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM AssetMaintenance m WHERE m.deleted = false " +
           "AND m.status = 'SCHEDULED' AND m.scheduledDate < :date")
    List<AssetMaintenance> findOverdueMaintenance(@Param("date") LocalDate date);

    @Query("SELECT m FROM AssetMaintenance m WHERE m.deleted = false AND m.assignedTo.id = :userId")
    Page<AssetMaintenance> findByAssignedTo(@Param("userId") UUID userId, Pageable pageable);
}
