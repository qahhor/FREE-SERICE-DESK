package com.servicedesk.monolith.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {
    private ReportType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String projectId;
    private List<String> teamIds;
    private List<String> agentIds;
    private List<String> channels;
    private List<String> priorities;
    private List<String> categories;
    private String groupBy; // day, week, month
    private String format; // json, csv, pdf, xlsx
    private Boolean includeDetails;
    private String locale;

    public enum ReportType {
        TICKET_SUMMARY,
        AGENT_PERFORMANCE,
        TEAM_PERFORMANCE,
        SLA_COMPLIANCE,
        CUSTOMER_SATISFACTION,
        CHANNEL_ANALYSIS,
        CATEGORY_ANALYSIS,
        RESPONSE_TIME,
        RESOLUTION_TIME,
        TICKET_VOLUME,
        CUSTOM
    }
}
