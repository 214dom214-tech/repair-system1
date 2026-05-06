package com.example.repairsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_history")
public class RequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requestId;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Column(nullable = false)
    private String changedBy;   // логин пользователя

    @Column(nullable = false)
    private String changeType;  // CREATED, EDITED, STATUS_CHANGED, SERVICE_CHANGED, CLOSED

    @Column(columnDefinition = "TEXT")
    private String details;     // JSON-строка с изменёнными полями

    public RequestHistory() {}

    public RequestHistory(Long requestId, String changedBy, String changeType, String details) {
        this.requestId = requestId;
        this.changedBy  = changedBy;
        this.changeType = changeType;
        this.details    = details;
        this.changedAt  = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public String getChangedBy() { return changedBy; }
    public String getChangeType() { return changeType; }
    public String getDetails() { return details; }
}
