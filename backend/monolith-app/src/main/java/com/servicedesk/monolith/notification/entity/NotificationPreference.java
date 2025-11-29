package com.servicedesk.monolith.notification.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    // Email notifications
    @Column(name = "email_enabled")
    private Boolean emailEnabled = true;

    @Column(name = "email_ticket_assigned")
    private Boolean emailTicketAssigned = true;

    @Column(name = "email_ticket_updated")
    private Boolean emailTicketUpdated = true;

    @Column(name = "email_ticket_commented")
    private Boolean emailTicketCommented = true;

    @Column(name = "email_ticket_resolved")
    private Boolean emailTicketResolved = true;

    @Column(name = "email_mentions")
    private Boolean emailMentions = true;

    @Column(name = "email_sla_alerts")
    private Boolean emailSlaAlerts = true;

    @Column(name = "email_daily_digest")
    private Boolean emailDailyDigest = false;

    @Column(name = "email_weekly_report")
    private Boolean emailWeeklyReport = true;

    // In-app notifications
    @Column(name = "inapp_enabled")
    private Boolean inappEnabled = true;

    @Column(name = "inapp_ticket_assigned")
    private Boolean inappTicketAssigned = true;

    @Column(name = "inapp_ticket_updated")
    private Boolean inappTicketUpdated = true;

    @Column(name = "inapp_ticket_commented")
    private Boolean inappTicketCommented = true;

    @Column(name = "inapp_mentions")
    private Boolean inappMentions = true;

    @Column(name = "inapp_sound")
    private Boolean inappSound = true;

    // Push notifications
    @Column(name = "push_enabled")
    private Boolean pushEnabled = false;

    @Column(name = "push_token")
    private String pushToken;

    // SMS notifications
    @Column(name = "sms_enabled")
    private Boolean smsEnabled = false;

    @Column(name = "sms_phone")
    private String smsPhone;

    @Column(name = "sms_urgent_only")
    private Boolean smsUrgentOnly = true;

    // Quiet hours
    @Column(name = "quiet_hours_enabled")
    private Boolean quietHoursEnabled = false;

    @Column(name = "quiet_hours_start")
    private String quietHoursStart = "22:00";

    @Column(name = "quiet_hours_end")
    private String quietHoursEnd = "08:00";

    @Column(name = "quiet_hours_timezone")
    private String quietHoursTimezone = "UTC";
}
