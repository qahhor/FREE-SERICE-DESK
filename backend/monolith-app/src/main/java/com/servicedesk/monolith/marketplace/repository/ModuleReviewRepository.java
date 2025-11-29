package com.servicedesk.monolith.marketplace.repository;

import com.servicedesk.monolith.marketplace.entity.ModuleReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleReviewRepository extends JpaRepository<ModuleReview, UUID> {

    @Query("SELECT mr FROM ModuleReview mr WHERE mr.moduleId = :moduleId AND mr.status = 'PUBLISHED' ORDER BY mr.createdAt DESC")
    Page<ModuleReview> findByModuleId(@Param("moduleId") String moduleId, Pageable pageable);

    Optional<ModuleReview> findByModuleIdAndUserId(String moduleId, UUID userId);

    @Query("SELECT AVG(mr.rating) FROM ModuleReview mr WHERE mr.moduleId = :moduleId AND mr.status = 'PUBLISHED'")
    Double calculateAverageRating(@Param("moduleId") String moduleId);

    @Query("SELECT COUNT(mr) FROM ModuleReview mr WHERE mr.moduleId = :moduleId AND mr.status = 'PUBLISHED'")
    int countByModuleId(@Param("moduleId") String moduleId);

    @Query("SELECT mr.rating, COUNT(mr) FROM ModuleReview mr WHERE mr.moduleId = :moduleId AND mr.status = 'PUBLISHED' GROUP BY mr.rating")
    java.util.List<Object[]> getRatingDistribution(@Param("moduleId") String moduleId);
}
