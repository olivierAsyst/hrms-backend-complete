package com.company.hrms.entity;

import com.company.hrms.enums.AbsenceStatus;
import com.company.hrms.enums.AbsenceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "absences", indexes = {
    @Index(name = "idx_absence_employee", columnList = "employee_id"),
    @Index(name = "idx_absence_type", columnList = "type"),
    @Index(name = "idx_absence_status", columnList = "status"),
    @Index(name = "idx_absence_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_absence_employee_status", columnList = "employee_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private AbsenceType type;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "is_half_day", nullable = false)
    @Builder.Default
    private Boolean isHalfDay = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AbsenceStatus status = AbsenceStatus.PENDING;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "supporting_document_url", length = 500)
    private String supportingDocumentUrl; // URL du justificatif

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean isPaid = true; // Congé payé ou non

    @Column(name = "deducted_from_balance", nullable = false)
    @Builder.Default
    private Boolean deductedFromBalance = true;

    @Column(name = "balance_before")
    private Integer balanceBefore; // Solde avant

    @Column(name = "balance_after")
    private Integer balanceAfter; // Solde après

    // Congé d'urgence
    @Column(name = "is_emergency", nullable = false)
    @Builder.Default
    private Boolean isEmergency = false;

    @Column(name = "emergency_contact", length = 200)
    private String emergencyContact;

    // Remplaçant pendant l'absence
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_employee_id")
    private Employee substituteEmployee;

    @Column(name = "substitute_notes", columnDefinition = "TEXT")
    private String substituteNotes;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Metadata en JSONB
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    // Méthodes utilitaires
    @Transient
    public boolean isApproved() {
        return status == AbsenceStatus.APPROVED;
    }

    @Transient
    public boolean isPending() {
        return status == AbsenceStatus.PENDING;
    }

    @Transient
    public boolean canBeModified() {
        return status == AbsenceStatus.PENDING || status == AbsenceStatus.REJECTED;
    }

    @Transient
    public Long getDurationInDays() {
        if (startDate == null || endDate == null) return 0L;
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
