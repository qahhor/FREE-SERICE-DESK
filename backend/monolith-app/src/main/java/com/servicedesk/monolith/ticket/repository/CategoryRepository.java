package com.servicedesk.monolith.ticket.repository;

import com.servicedesk.monolith.ticket.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT c FROM Category c WHERE c.project.id = :projectId AND c.deleted = false ORDER BY c.sortOrder")
    List<Category> findByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT c FROM Category c WHERE c.project.id = :projectId AND c.parent IS NULL " +
           "AND c.deleted = false ORDER BY c.sortOrder")
    List<Category> findRootCategoriesByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.deleted = false ORDER BY c.sortOrder")
    List<Category> findByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT c FROM Category c WHERE c.project.id = :projectId AND c.name = :name AND c.deleted = false")
    Optional<Category> findByProjectIdAndName(@Param("projectId") UUID projectId, @Param("name") String name);
}
