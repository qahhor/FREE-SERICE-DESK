package com.servicedesk.ticket.repository;

import com.servicedesk.ticket.entity.User;
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
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.status = 'ACTIVE' " +
           "AND (u.role = 'AGENT' OR u.role = 'MANAGER' OR u.role = 'ADMIN')")
    List<User> findAllActiveAgents();

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.team.id = :teamId")
    List<User> findByTeamId(@Param("teamId") UUID teamId);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.status = :status")
    Page<User> findByStatus(@Param("status") User.UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.role = :role")
    Page<User> findByRole(@Param("role") User.UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.projects p WHERE p.id = :projectId AND u.deleted = false")
    List<User> findByProjectId(@Param("projectId") UUID projectId);
}
