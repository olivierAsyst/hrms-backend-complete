package com.company.hrms.entity;

import com.company.hrms.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "salary_advances", indexes = {
    @Index(name = "idx_salary_advance_employee", columnList = "employee_id"),
    @Index(name = "idx_salary_advance_status", columnList = "status"),
    @Index(name = "idx_salary_advance_request_date", columnList = "request_date"),
    @Index(name = "idx_salary_advance_repayment", columnList = "fully_repaid")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryAdvance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "amount_requested", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountRequested;

    @Column(name = "amount_approved", precision = 15, scale = 2)
    private BigDecimal amountApproved;

    @Column(name = "amount_disbursed", precision = 15, scale = 2)
    private BigDecimal amountDisbursed;

    @Column(name = "amount_repaid", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountRepaid = BigDecimal.ZERO;

    @Column(name = "remaining_balance", precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PayrollStatus status = PayrollStatus.PENDING;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason; // Raison de la demande

    @Column(name = "repayment_months", nullable = false)
    private Integer repaymentMonths; // Nombre de mois pour le remboursement

    @Column(name = "monthly_repayment", precision = 15, scale = 2)
    private BigDecimal monthlyRepayment; // Montant mensuel à déduire

    @Column(name = "repayment_start_date")
    private LocalDate repaymentStartDate;

    @Column(name = "repayment_end_date")
    private LocalDate repaymentEndDate;

    @Column(name = "fully_repaid", nullable = false)
    @Builder.Default
    private Boolean fullyRepaid = false;

    @Column(name = "repayment_completed_date")
    private LocalDate repaymentCompletedDate;

    // Approbation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    // Rejet
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private User rejectedBy;

    @Column(name = "rejected_at")
    private java.time.LocalDateTime rejectedAt;

    // Décaissement
    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "disbursement_method", length = 50)
    private String disbursementMethod; // Bank Transfer, Cash, Cheque

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursed_by")
    private User disbursedBy;

    @Column(name = "disbursed_at")
    private java.time.LocalDateTime disbursedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Détails supplémentaires en JSONB
    @Column(name = "repayment_schedule", columnDefinition = "jsonb")
    private String repaymentSchedule; // Planning détaillé des remboursements

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    // Méthodes utilitaires
    @Transient
    public void calculateMonthlyRepayment() {
        if (amountApproved != null && repaymentMonths != null && repaymentMonths > 0) {
            this.monthlyRepayment = amountApproved.divide(
                BigDecimal.valueOf(repaymentMonths),
                2,
                java.math.RoundingMode.HALF_UP
            );
        }
    }

    @Transient
    public void calculateRemainingBalance() {
        if (amountDisbursed != null && amountRepaid != null) {
            this.remainingBalance = amountDisbursed.subtract(amountRepaid);
            if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                this.fullyRepaid = true;
                this.repaymentCompletedDate = LocalDate.now();
            }
        }
    }

    @Transient
    public void recordRepayment(BigDecimal amount) {
        if (amountRepaid == null) {
            amountRepaid = BigDecimal.ZERO;
        }
        this.amountRepaid = amountRepaid.add(amount);
        calculateRemainingBalance();
    }

    @Transient
    public boolean canBeModified() {
        return status == PayrollStatus.PENDING || status == PayrollStatus.DRAFT;
    }

    @Transient
    public boolean isApproved() {
        return status == PayrollStatus.APPROVED || status == PayrollStatus.PAID;
    }

    @Transient
    public BigDecimal getRepaymentProgress() {
        if (amountDisbursed == null || amountDisbursed.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amountRepaid.divide(amountDisbursed, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
