package com.servicedesk.monolith.analytics.service;

import com.servicedesk.monolith.analytics.dto.DashboardMetrics;
import com.servicedesk.monolith.analytics.dto.ReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardMetrics getDashboardMetrics(String projectId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching dashboard metrics for project {} from {} to {}", projectId, startDate, endDate);

        DashboardMetrics metrics = DashboardMetrics.builder()
                .totalTickets(getTotalTickets(projectId, startDate, endDate))
                .openTickets(getTicketsByStatus(projectId, "OPEN"))
                .pendingTickets(getTicketsByStatus(projectId, "PENDING"))
                .resolvedTickets(getTicketsByStatus(projectId, "RESOLVED"))
                .closedTickets(getTicketsByStatus(projectId, "CLOSED"))
                .averageResponseTime(getAverageResponseTime(projectId, startDate, endDate))
                .averageResolutionTime(getAverageResolutionTime(projectId, startDate, endDate))
                .firstResponseTime(getFirstResponseTime(projectId, startDate, endDate))
                .customerSatisfactionScore(getCSAT(projectId, startDate, endDate))
                .netPromoterScore(getNPS(projectId, startDate, endDate))
                .slaComplianceRate(getSlaComplianceRate(projectId, startDate, endDate))
                .slaBreaches(getSlaBreaches(projectId, startDate, endDate))
                .ticketsCreatedToday(getTicketsCreatedToday(projectId))
                .ticketsResolvedToday(getTicketsResolvedToday(projectId))
                .activeAgents(getActiveAgents(projectId))
                .ticketsByChannel(getTicketsByChannel(projectId, startDate, endDate))
                .ticketsByPriority(getTicketsByPriority(projectId, startDate, endDate))
                .ticketsByStatus(getTicketsByStatusMap(projectId))
                .ticketsByCategory(getTicketsByCategory(projectId, startDate, endDate))
                .ticketVolumeTrend(getTicketVolumeTrend(projectId, startDate, endDate))
                .resolutionTimeTrend(getResolutionTimeTrend(projectId, startDate, endDate))
                .satisfactionTrend(getSatisfactionTrend(projectId, startDate, endDate))
                .build();

        return metrics;
    }

    public List<Map<String, Object>> getAgentPerformance(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT
                u.id as agent_id,
                u.first_name || ' ' || u.last_name as agent_name,
                COUNT(t.id) as total_tickets,
                COUNT(CASE WHEN t.status = 'RESOLVED' OR t.status = 'CLOSED' THEN 1 END) as resolved_tickets,
                AVG(EXTRACT(EPOCH FROM (t.first_response_at - t.created_at))/60) as avg_response_time,
                AVG(EXTRACT(EPOCH FROM (t.resolved_at - t.created_at))/3600) as avg_resolution_time,
                AVG(t.satisfaction_rating) as avg_rating
            FROM users u
            LEFT JOIN tickets t ON t.assignee_id = u.id
                AND t.created_at BETWEEN ? AND ?
            WHERE u.role = 'AGENT'
            GROUP BY u.id, u.first_name, u.last_name
            ORDER BY resolved_tickets DESC
            """;

        return jdbcTemplate.queryForList(sql, startDate, endDate.plusDays(1));
    }

    public List<Map<String, Object>> getTeamPerformance(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT
                tm.id as team_id,
                tm.name as team_name,
                COUNT(t.id) as total_tickets,
                COUNT(CASE WHEN t.status = 'RESOLVED' OR t.status = 'CLOSED' THEN 1 END) as resolved_tickets,
                AVG(EXTRACT(EPOCH FROM (t.first_response_at - t.created_at))/60) as avg_response_time,
                COUNT(CASE WHEN t.sla_breached = true THEN 1 END) as sla_breaches
            FROM teams tm
            LEFT JOIN tickets t ON t.team_id = tm.id
                AND t.created_at BETWEEN ? AND ?
            GROUP BY tm.id, tm.name
            ORDER BY resolved_tickets DESC
            """;

        return jdbcTemplate.queryForList(sql, startDate, endDate.plusDays(1));
    }

    public Map<String, Object> getSlaReport(String projectId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        // Overall SLA compliance
        report.put("complianceRate", getSlaComplianceRate(projectId, startDate, endDate));
        report.put("totalBreaches", getSlaBreaches(projectId, startDate, endDate));

        // SLA by priority
        String sqlByPriority = """
            SELECT
                priority,
                COUNT(*) as total,
                COUNT(CASE WHEN sla_breached = false THEN 1 END) as compliant,
                ROUND(COUNT(CASE WHEN sla_breached = false THEN 1 END)::decimal / COUNT(*) * 100, 2) as compliance_rate
            FROM tickets
            WHERE created_at BETWEEN ? AND ?
            GROUP BY priority
            """;
        report.put("byPriority", jdbcTemplate.queryForList(sqlByPriority, startDate, endDate.plusDays(1)));

        // SLA by category
        String sqlByCategory = """
            SELECT
                c.name as category,
                COUNT(*) as total,
                COUNT(CASE WHEN t.sla_breached = false THEN 1 END) as compliant,
                ROUND(COUNT(CASE WHEN t.sla_breached = false THEN 1 END)::decimal / COUNT(*) * 100, 2) as compliance_rate
            FROM tickets t
            LEFT JOIN categories c ON t.category_id = c.id
            WHERE t.created_at BETWEEN ? AND ?
            GROUP BY c.name
            """;
        report.put("byCategory", jdbcTemplate.queryForList(sqlByCategory, startDate, endDate.plusDays(1)));

        return report;
    }

    public byte[] generateReport(ReportRequest request) {
        // Generate report based on type and format
        // This would typically involve using Apache POI for Excel, iText for PDF, etc.
        log.info("Generating {} report in {} format", request.getType(), request.getFormat());

        // For now, return empty bytes - in production, implement actual report generation
        return new byte[0];
    }

    // Private helper methods

    private Long getTotalTickets(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE created_at BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate.plusDays(1));
    }

    private Long getTicketsByStatus(String projectId, String status) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE status = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, status);
    }

    private Double getAverageResponseTime(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT AVG(EXTRACT(EPOCH FROM (first_response_at - created_at))/60)
            FROM tickets
            WHERE first_response_at IS NOT NULL
            AND created_at BETWEEN ? AND ?
            """;
        Double result = jdbcTemplate.queryForObject(sql, Double.class, startDate, endDate.plusDays(1));
        return result != null ? Math.round(result * 10.0) / 10.0 : 0.0;
    }

    private Double getAverageResolutionTime(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT AVG(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600)
            FROM tickets
            WHERE resolved_at IS NOT NULL
            AND created_at BETWEEN ? AND ?
            """;
        Double result = jdbcTemplate.queryForObject(sql, Double.class, startDate, endDate.plusDays(1));
        return result != null ? Math.round(result * 10.0) / 10.0 : 0.0;
    }

    private Double getFirstResponseTime(String projectId, LocalDate startDate, LocalDate endDate) {
        return getAverageResponseTime(projectId, startDate, endDate);
    }

    private Double getCSAT(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT AVG(satisfaction_rating) * 20
            FROM tickets
            WHERE satisfaction_rating IS NOT NULL
            AND created_at BETWEEN ? AND ?
            """;
        Double result = jdbcTemplate.queryForObject(sql, Double.class, startDate, endDate.plusDays(1));
        return result != null ? Math.round(result * 10.0) / 10.0 : 0.0;
    }

    private Double getNPS(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT
                (COUNT(CASE WHEN satisfaction_rating >= 4 THEN 1 END)::decimal / NULLIF(COUNT(*), 0) * 100) -
                (COUNT(CASE WHEN satisfaction_rating <= 2 THEN 1 END)::decimal / NULLIF(COUNT(*), 0) * 100)
            FROM tickets
            WHERE satisfaction_rating IS NOT NULL
            AND created_at BETWEEN ? AND ?
            """;
        Double result = jdbcTemplate.queryForObject(sql, Double.class, startDate, endDate.plusDays(1));
        return result != null ? Math.round(result * 10.0) / 10.0 : 0.0;
    }

    private Double getSlaComplianceRate(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT ROUND(COUNT(CASE WHEN sla_breached = false THEN 1 END)::decimal / NULLIF(COUNT(*), 0) * 100, 2)
            FROM tickets
            WHERE created_at BETWEEN ? AND ?
            """;
        Double result = jdbcTemplate.queryForObject(sql, Double.class, startDate, endDate.plusDays(1));
        return result != null ? result : 100.0;
    }

    private Long getSlaBreaches(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COUNT(*)
            FROM tickets
            WHERE sla_breached = true
            AND created_at BETWEEN ? AND ?
            """;
        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate.plusDays(1));
    }

    private Long getTicketsCreatedToday(String projectId) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE DATE(created_at) = CURRENT_DATE";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private Long getTicketsResolvedToday(String projectId) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE DATE(resolved_at) = CURRENT_DATE";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private Long getActiveAgents(String projectId) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'AGENT' AND status = 'ACTIVE'";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private Map<String, Long> getTicketsByChannel(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT channel, COUNT(*) as count
            FROM tickets
            WHERE created_at BETWEEN ? AND ?
            GROUP BY channel
            """;
        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("channel"), rs.getLong("count"));
            }
            return result;
        }, startDate, endDate.plusDays(1));
    }

    private Map<String, Long> getTicketsByPriority(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT priority, COUNT(*) as count
            FROM tickets
            WHERE created_at BETWEEN ? AND ?
            GROUP BY priority
            """;
        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("priority"), rs.getLong("count"));
            }
            return result;
        }, startDate, endDate.plusDays(1));
    }

    private Map<String, Long> getTicketsByStatusMap(String projectId) {
        String sql = "SELECT status, COUNT(*) as count FROM tickets GROUP BY status";
        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("status"), rs.getLong("count"));
            }
            return result;
        });
    }

    private Map<String, Long> getTicketsByCategory(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COALESCE(c.name, 'Uncategorized') as category, COUNT(*) as count
            FROM tickets t
            LEFT JOIN categories c ON t.category_id = c.id
            WHERE t.created_at BETWEEN ? AND ?
            GROUP BY c.name
            """;
        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("category"), rs.getLong("count"));
            }
            return result;
        }, startDate, endDate.plusDays(1));
    }

    private List<DashboardMetrics.TimeSeriesData> getTicketVolumeTrend(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT DATE(created_at) as date, COUNT(*) as value
            FROM tickets
            WHERE created_at BETWEEN ? AND ?
            GROUP BY DATE(created_at)
            ORDER BY date
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                DashboardMetrics.TimeSeriesData.builder()
                        .date(rs.getDate("date").toLocalDate())
                        .value(rs.getDouble("value"))
                        .label("Tickets")
                        .build(),
                startDate, endDate.plusDays(1));
    }

    private List<DashboardMetrics.TimeSeriesData> getResolutionTimeTrend(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT DATE(resolved_at) as date,
                   AVG(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as value
            FROM tickets
            WHERE resolved_at IS NOT NULL
            AND resolved_at BETWEEN ? AND ?
            GROUP BY DATE(resolved_at)
            ORDER BY date
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                DashboardMetrics.TimeSeriesData.builder()
                        .date(rs.getDate("date").toLocalDate())
                        .value(rs.getDouble("value"))
                        .label("Hours")
                        .build(),
                startDate, endDate.plusDays(1));
    }

    private List<DashboardMetrics.TimeSeriesData> getSatisfactionTrend(String projectId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT DATE(created_at) as date, AVG(satisfaction_rating) * 20 as value
            FROM tickets
            WHERE satisfaction_rating IS NOT NULL
            AND created_at BETWEEN ? AND ?
            GROUP BY DATE(created_at)
            ORDER BY date
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                DashboardMetrics.TimeSeriesData.builder()
                        .date(rs.getDate("date").toLocalDate())
                        .value(rs.getDouble("value"))
                        .label("CSAT")
                        .build(),
                startDate, endDate.plusDays(1));
    }
}
