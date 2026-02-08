package com.company.hrms.entity;

import com.company.hrms.enums.SalaryTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "salary_transactions", indexes = {
    @Index(name = "idx_salary_transaction_employee", columnList = "employee_id"),
    @Index(name = "idx_salary_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_salary_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_salary_transaction_payroll", columnList = "payroll_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private SalaryTransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_credit", nullable = false)
    private Boolean isCredit; // true = crédit (ajout), false = débit (déduction)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    // Lien vers la paie si applicable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id")
    private Payroll payroll;

    // Lien vers l'avance sur salaire si applicable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_advance_id")
    private SalaryAdvance salaryAdvance;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Column(name = "processed_at")
    private java.time.LocalDateTime processedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Metadata en JSONB
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    // Méthodes utilitaires
    @Transient
    public String getTransactionTypeDisplay() {
        return transactionType.name().replace("_", " ");
    }

    @Transient
    public String getAmountWithSign() {
        String sign = isCredit ? "+" : "-";
        return sign + amount.toString();
    }
}
