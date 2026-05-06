package com.example.repairsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;        // содержание заявки
    private String description;
    private String note;

    // Новые поля
    private String department;        // подразделение
    private String applicantName;     // ФИО заявителя
    private String applicant;         // логин / должность заявителя

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    private RequestPriority priority = RequestPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType = ServiceType.UNKNOWN;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    private LocalDateTime acceptedAt;
    private LocalDateTime closedAt;

    // Реальный простой оборудования (в минутах)
    private Integer downtimeMinutes;

    // Transient — только для передачи из фронтенда, не сохраняется в БД
    @Transient
    private Boolean sendSms;

    @ManyToOne
    @JoinColumn(name = "equipment_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","section"})
    private Equipment equipment;

    public Request() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicant() { return applicant; }
    public void setApplicant(String applicant) { this.applicant = applicant; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public RequestPriority getPriority() { return priority; }
    public void setPriority(RequestPriority priority) {
        this.priority = priority != null ? priority : RequestPriority.NORMAL;
    }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType != null ? serviceType : ServiceType.UNKNOWN;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public Integer getDowntimeMinutes() { return downtimeMinutes; }
    public void setDowntimeMinutes(Integer downtimeMinutes) { this.downtimeMinutes = downtimeMinutes; }

    public Boolean getSendSms() { return sendSms; }
    public void setSendSms(Boolean sendSms) { this.sendSms = sendSms; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
}
