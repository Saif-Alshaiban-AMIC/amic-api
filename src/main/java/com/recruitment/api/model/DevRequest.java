package com.recruitment.api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "dev_requests")
public class DevRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requesterName;

    @Column(nullable = false)
    private String requesterEmail;

    private String department;

    @Column(nullable = false)
    private String appName;

    @Column(nullable = false)
    private String appType;          // WEB_APP | MOBILE_APP | DESKTOP_APP | API_INTEGRATION | DASHBOARD | OTHER

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String priority;         // LOW | MEDIUM | HIGH | CRITICAL

    private LocalDate targetDate;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING | IN_REVIEW | APPROVED | IN_PROGRESS | COMPLETED | REJECTED

    @Column(columnDefinition = "TEXT")
    private String notes;            // IT team notes

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String v) { this.requesterName = v; }
    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String v) { this.requesterEmail = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String v) { this.department = v; }
    public String getAppName() { return appName; }
    public void setAppName(String v) { this.appName = v; }
    public String getAppType() { return appType; }
    public void setAppType(String v) { this.appType = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getPriority() { return priority; }
    public void setPriority(String v) { this.priority = v; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate v) { this.targetDate = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
