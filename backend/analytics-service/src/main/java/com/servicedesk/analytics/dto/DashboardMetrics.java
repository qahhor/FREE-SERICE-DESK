package com.servicedesk.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetrics {

    // Overview metrics
    private Long totalTickets;
    private Long openTickets;
    private Long pendingTickets;
    private Long resolvedTickets;
    private Long closedTickets;

    // Performance metrics
    private Double averageResponseTime; // in minutes
    private Double averageResolutionTime; // in hours
    private Double firstResponseTime; // in minutes
    private Double customerSatisfactionScore; // CSAT 0-100
    private Double netPromoterScore; // NPS -100 to 100

    // SLA metrics
    private Double slaComplianceRate; // percentage
    private Long slaBreaches;
    private Long slaAtRisk;

    // Volume metrics
    private Long ticketsCreatedToday;
    private Long ticketsResolvedToday;
    private Double ticketVolumeChange; // percentage change from previous period

    // Agent metrics
    private Long activeAgents;
    private Double averageTicketsPerAgent;
    private AgentPerformance topAgent;

    // Channel distribution
    private Map<String, Long> ticketsByChannel;
    private Map<String, Long> ticketsByPriority;
    private Map<String, Long> ticketsByStatus;
    private Map<String, Long> ticketsByCategory;

    // Trends
    private List<TimeSeriesData> ticketVolumeTrend;
    private List<TimeSeriesData> resolutionTimeTrend;
    private List<TimeSeriesData> satisfactionTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSeriesData {
        private LocalDate date;
        private Double value;
        private String label;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgentPerformance {
        private String agentId;
        private String agentName;
        private Long ticketsResolved;
        private Double averageRating;
        private Double averageResponseTime;
    }
}
