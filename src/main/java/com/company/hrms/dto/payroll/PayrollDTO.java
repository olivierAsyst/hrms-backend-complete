package com.company.hrms.dto.payroll;

import com.company.hrms.enums.PayrollStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDTO {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    private String employeeName;
    private String employeeNumber;
    private String departmentName;

    @NotBlank(message = "Pay period is required")
    private String payPeriod; // Format: YYYY-MM

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @NotNull(message = "Status is required")
    private PayrollStatus status;

    // Earnings
    @NotNull(message = "Basic salary is required")
    @DecimalMin(value = "0.0", message = "Basic salary must be positive")
    private BigDecimal basicSalary;
    
    private BigDecimal housingAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal mealAllowance;
    private BigDecimal bonus;
    private BigDecimal commission;
    private BigDecimal overtimePay;
    private BigDecimal otherEarnings;

    // Deductions
    private BigDecimal taxDeduction;
    private BigDecimal socialSecurity;
    private BigDecimal healthInsurance;
    private BigDecimal pensionContribution;
    private BigDecimal loanRepayment;
    private BigDecimal salaryAdvanceDeduction;
    private BigDecimal absenceDeduction;
    private BigDecimal lateDeduction;
    private BigDecimal otherDeductions;

    // Calculated totals
    private BigDecimal grossSalary;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;

    // Attendance details
    private Integer workingDays;
    private Integer presentDays;
    private Integer absentDays;
    private Integer paidLeaveDays;
    private Integer unpaidLeaveDays;
    private BigDecimal overtimeHours;
    private BigDecimal lateHours;

    private String notes;

    // Payment details
    private String paymentMethod;
    private String bankAccountNumber;
    private String transactionReference;

    // Approval
    private Long approvedBy;
    private String approvedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    private Long paidBy;
    private String paidByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;

    // Breakdowns (JSONB)
    private String earningsBreakdown;
    private String deductionsBreakdown;

    private String payslipUrl;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;

    // Computed fields
    private Boolean canBeModified;
}
