package com.servicedesk.monolith.ticket.repository;

import com.servicedesk.monolith.ticket.entity.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, UUID> {

    @Query("SELECT c FROM AssetCategory c WHERE c.deleted = false AND c.parent IS NULL ORDER BY c.sortOrder")
    List<AssetCategory> findRootCategories();

    @Query("SELECT c FROM AssetCategory c WHERE c.deleted = false AND c.parent.id = :parentId ORDER BY c.sortOrder")
    List<AssetCategory> findByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT c FROM AssetCategory c WHERE c.deleted = false ORDER BY c.sortOrder")
    List<AssetCategory> findAllActive();
}
