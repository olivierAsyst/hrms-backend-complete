package com.company.hrms.entity;

import com.company.hrms.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "payrolls", indexes = {
    @Index(name = "idx_payroll_employee", columnList = "employee_id"),
    @Index(name = "idx_payroll_period", columnList = "pay_period"),
    @Index(name = "idx_payroll_status", columnList = "status"),
    @Index(name = "idx_payroll_employee_period", columnList = "employee_id, pay_period"),
    @Index(name = "idx_payroll_payment_date", columnList = "payment_date")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uk_employee_period", columnNames = {"employee_id", "pay_period"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_period", nullable = false, length = 7) // Format: YYYY-MM
    private String payPeriod; // Ex: "2024-01"

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PayrollStatus status = PayrollStatus.DRAFT;

    // Salaire de base
    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    // Allocations et primes
    @Column(name = "housing_allowance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal housingAllowance = BigDecimal.ZERO;

    @Column(name = "transport_allowance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal transportAllowance = BigDecimal.ZERO;

    @Column(name = "meal_allowance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal mealAllowance = BigDecimal.ZERO;

    @Column(name = "bonus", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "commission", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(name = "overtime_pay", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Column(name = "other_earnings", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal otherEarnings = BigDecimal.ZERO;

    // Déductions
    @Column(name = "tax_deduction", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxDeduction = BigDecimal.ZERO;

    @Column(name = "social_security", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal socialSecurity = BigDecimal.ZERO;

    @Column(name = "health_insurance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal healthInsurance = BigDecimal.ZERO;

    @Column(name = "pension_contribution", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal pensionContribution = BigDecimal.ZERO;

    @Column(name = "loan_repayment", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal loanRepayment = BigDecimal.ZERO;

    @Column(name = "salary_advance_deduction", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal salaryAdvanceDeduction = BigDecimal.ZERO;

    @Column(name = "absence_deduction", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal absenceDeduction = BigDecimal.ZERO;

    @Column(name = "late_deduction", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal lateDeduction = BigDecimal.ZERO;

    @Column(name = "other_deductions", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    // Totaux calculés
    @Column(name = "gross_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossSalary; // Salaire brut

    @Column(name = "total_deductions", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "net_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary; // Salaire net

    // Détails de présence
    @Column(name = "working_days", nullable = false)
    private Integer workingDays;

    @Column(name = "present_days", nullable = false)
    private Integer presentDays;

    @Column(name = "absent_days", nullable = false)
    @Builder.Default
    private Integer absentDays = 0;

    @Column(name = "paid_leave_days", nullable = false)
    @Builder.Default
    private Integer paidLeaveDays = 0;

    @Column(name = "unpaid_leave_days", nullable = false)
    @Builder.Default
    private Integer unpaidLeaveDays = 0;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "late_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal lateHours = BigDecimal.ZERO;

    // Notes et détails
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Détails de paiement
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Bank Transfer, Cash, Cheque

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    // Approbation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by")
    private User paidBy;

    @Column(name = "paid_at")
    private java.time.LocalDateTime paidAt;

    // Détails supplémentaires en JSONB
    @Column(name = "earnings_breakdown", columnDefinition = "jsonb")
    private String earningsBreakdown;

    @Column(name = "deductions_breakdown", columnDefinition = "jsonb")
    private String deductionsBreakdown;

    // Fichier de paie généré
    @Column(name = "payslip_url", length = 500)
    private String payslipUrl;

    // Méthodes de calcul
    @Transient
    public void calculateGrossSalary() {
        this.grossSalary = basicSalary
            .add(housingAllowance)
            .add(transportAllowance)
            .add(mealAllowance)
            .add(bonus)
            .add(commission)
            .add(overtimePay)
            .add(otherEarnings);
    }

    @Transient
    public void calculateTotalDeductions() {
        this.totalDeductions = taxDeduction
            .add(socialSecurity)
            .add(healthInsurance)
            .add(pensionContribution)
            .add(loanRepayment)
            .add(salaryAdvanceDeduction)
            .add(absenceDeduction)
            .add(lateDeduction)
            .add(otherDeductions);
    }

    @Transient
    public void calculateNetSalary() {
        calculateGrossSalary();
        calculateTotalDeductions();
        this.netSalary = grossSalary.subtract(totalDeductions);
    }

    @Transient
    public YearMonth getPayPeriodAsYearMonth() {
        return YearMonth.parse(payPeriod);
    }

    @Transient
    public boolean canBeModified() {
        return status == PayrollStatus.DRAFT || status == PayrollStatus.PENDING;
    }

    @Transient
    public boolean isPaid() {
        return status == PayrollStatus.PAID;
    }
}
