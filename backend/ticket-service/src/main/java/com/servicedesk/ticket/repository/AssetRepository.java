package com.servicedesk.ticket.repository;

import com.servicedesk.ticket.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {

    Optional<Asset> findByAssetTag(String assetTag);

    Optional<Asset> findByIdAndDeletedFalse(UUID id);

    Optional<Asset> findByAssetTagAndDeletedFalse(String assetTag);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false")
    Page<Asset> findAllActive(Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND a.status = :status")
    Page<Asset> findByStatus(@Param("status") Asset.AssetStatus status, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND a.type = :type")
    Page<Asset> findByType(@Param("type") Asset.AssetType type, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND a.category.id = :categoryId")
    Page<Asset> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND a.owner.id = :ownerId")
    Page<Asset> findByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND a.project.id = :projectId")
    Page<Asset> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.assetTag) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.hostname) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Asset> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND a.warrantyExpiry < :date")
    List<Asset> findWithWarrantyExpiringBefore(@Param("date") LocalDate date);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND a.licenseExpiry < :date")
    List<Asset> findWithLicenseExpiringBefore(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false AND a.status = :status")
    long countByStatus(@Param("status") Asset.AssetStatus status);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false AND a.type = :type")
    long countByType(@Param("type") Asset.AssetType type);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false AND a.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT a FROM Asset a JOIN a.linkedTickets t WHERE t.id = :ticketId AND a.deleted = false")
    List<Asset> findByTicketId(@Param("ticketId") UUID ticketId);

    @Query("SELECT COALESCE(SUM(a.purchaseCost), 0) FROM Asset a WHERE a.deleted = false")
    java.math.BigDecimal getTotalPurchaseCost();

    @Query("SELECT COALESCE(SUM(a.currentValue), 0) FROM Asset a WHERE a.deleted = false")
    java.math.BigDecimal getTotalCurrentValue();
}
