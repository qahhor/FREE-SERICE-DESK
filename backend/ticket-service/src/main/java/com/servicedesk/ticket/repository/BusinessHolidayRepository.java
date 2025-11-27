package com.servicedesk.ticket.repository;

import com.servicedesk.ticket.entity.BusinessHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessHolidayRepository extends JpaRepository<BusinessHoliday, UUID> {

    @Query("SELECT h FROM BusinessHoliday h WHERE h.deleted = false " +
           "AND (h.project IS NULL OR h.project.id = :projectId) " +
           "AND h.holidayDate BETWEEN :startDate AND :endDate")
    List<BusinessHoliday> findHolidaysInRange(
            @Param("projectId") UUID projectId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM BusinessHoliday h WHERE h.deleted = false " +
           "AND (h.project IS NULL OR h.project.id = :projectId) " +
           "AND h.holidayDate = :date")
    List<BusinessHoliday> findByDate(@Param("projectId") UUID projectId, @Param("date") LocalDate date);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM BusinessHoliday h " +
           "WHERE h.deleted = false AND (h.project IS NULL OR h.project.id = :projectId) " +
           "AND h.holidayDate = :date")
    boolean isHoliday(@Param("projectId") UUID projectId, @Param("date") LocalDate date);

    @Query("SELECT h FROM BusinessHoliday h WHERE h.deleted = false AND h.isRecurring = true")
    List<BusinessHoliday> findAllRecurring();
}
