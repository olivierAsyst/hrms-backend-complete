package com.company.hrms.entity;

import com.company.hrms.enums.AbsenceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_balances", indexes = {
    @Index(name = "idx_leave_balance_employee", columnList = "employee_id"),
    @Index(name = "idx_leave_balance_year", columnList = "year"),
    @Index(name = "idx_leave_balance_employee_year", columnList = "employee_id, year"),
    @Index(name = "idx_leave_balance_type", columnList = "leave_type")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uk_employee_year_type", columnNames = {"employee_id", "year", "leave_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 30)
    private AbsenceType leaveType;

    @Column(name = "total_allocated", nullable = false)
    @Builder.Default
    private Integer totalAllocated = 0; // Total alloué pour l'année

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Integer used = 0; // Jours utilisés

    @Column(name = "pending", nullable = false)
    @Builder.Default
    private Integer pending = 0; // Jours en attente d'approbation

    @Column(name = "available", nullable = false)
    @Builder.Default
    private Integer available = 0; // Jours disponibles

    @Column(name = "carried_forward")
    @Builder.Default
    private Integer carriedForward = 0; // Report de l'année précédente

    @Column(name = "carry_forward_limit")
    private Integer carryForwardLimit; // Limite de report

    @Column(name = "expires_on")
    private java.time.LocalDate expiresOn; // Date d'expiration du report

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Méthodes utilitaires
    @Transient
    public Integer getRemainingBalance() {
        return available;
    }

    @Transient
    public Integer getTotalUsedAndPending() {
        return used + pending;
    }

    @Transient
    public boolean hasAvailableLeave(Integer requestedDays) {
        return available >= requestedDays;
    }

    @Transient
    public void updateBalance() {
        this.available = totalAllocated + carriedForward - used - pending;
    }

    // Méthode pour incrémenter l'utilisation
    public void incrementUsed(Integer days) {
        this.used += days;
        updateBalance();
    }

    // Méthode pour incrémenter le pending
    public void incrementPending(Integer days) {
        this.pending += days;
        updateBalance();
    }

    // Méthode pour décrémenter le pending
    public void decrementPending(Integer days) {
        this.pending -= days;
        updateBalance();
    }

    // Approuver une absence
    public void approveLeave(Integer days) {
        this.pending -= days;
        this.used += days;
        updateBalance();
    }

    // Rejeter une absence
    public void rejectLeave(Integer days) {
        this.pending -= days;
        updateBalance();
    }
}
