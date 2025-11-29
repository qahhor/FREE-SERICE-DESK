package com.servicedesk.monolith.marketplace.repository;

import com.servicedesk.monolith.marketplace.entity.MarketplaceModule;
import com.servicedesk.monolith.marketplace.plugin.ModuleCategory;
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
public interface MarketplaceModuleRepository extends JpaRepository<MarketplaceModule, UUID> {

    Optional<MarketplaceModule> findByModuleId(String moduleId);

    boolean existsByModuleId(String moduleId);

    @Query("""
        SELECT m FROM MarketplaceModule m
        WHERE m.status = 'PUBLISHED'
        AND (:query IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:category IS NULL OR m.category = :category)
        AND (:freeOnly = false OR m.pricingModel = 'FREE')
        AND (:verifiedOnly = false OR m.verified = true)
        AND (:minRating IS NULL OR m.averageRating >= :minRating)
        ORDER BY m.installCount DESC
    """)
    Page<MarketplaceModule> searchModules(
            @Param("query") String query,
            @Param("category") ModuleCategory category,
            @Param("freeOnly") boolean freeOnly,
            @Param("verifiedOnly") boolean verifiedOnly,
            @Param("minRating") Double minRating,
            Pageable pageable
    );

    @Query("SELECT m FROM MarketplaceModule m WHERE m.status = 'PUBLISHED' AND m.featured = true ORDER BY m.installCount DESC")
    List<MarketplaceModule> findFeaturedModules(Pageable pageable);

    @Query("SELECT m FROM MarketplaceModule m WHERE m.status = 'PUBLISHED' ORDER BY m.createdAt DESC")
    List<MarketplaceModule> findNewestModules(Pageable pageable);

    @Query("SELECT m FROM MarketplaceModule m WHERE m.status = 'PUBLISHED' ORDER BY m.installCount DESC")
    List<MarketplaceModule> findPopularModules(Pageable pageable);

    @Query("SELECT m FROM MarketplaceModule m WHERE m.status = 'PUBLISHED' AND m.category = :category ORDER BY m.installCount DESC")
    Page<MarketplaceModule> findByCategory(@Param("category") ModuleCategory category, Pageable pageable);

    @Query("SELECT m FROM MarketplaceModule m WHERE m.status = 'PUBLISHED' AND m.official = true ORDER BY m.name")
    List<MarketplaceModule> findOfficialModules();

    @Query("SELECT DISTINCT m.category FROM MarketplaceModule m WHERE m.status = 'PUBLISHED'")
    List<ModuleCategory> findActiveCategories();

    @Query("SELECT COUNT(m) FROM MarketplaceModule m WHERE m.status = 'PUBLISHED' AND m.category = :category")
    long countByCategory(@Param("category") ModuleCategory category);
}
