package com.company.hrms.dto.payroll;

import com.company.hrms.enums.PayrollStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class SalaryAdvanceDTO {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    private String employeeName;
    private String employeeNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestDate;

    @NotNull(message = "Amount requested is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amountRequested;

    private BigDecimal amountApproved;
    private BigDecimal amountDisbursed;
    private BigDecimal amountRepaid;
    private BigDecimal remainingBalance;

    private PayrollStatus status;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotNull(message = "Repayment months is required")
    @Min(value = 1, message = "Repayment months must be at least 1")
    private Integer repaymentMonths;

    private BigDecimal monthlyRepayment;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate repaymentStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate repaymentEndDate;

    private Boolean fullyRepaid;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate repaymentCompletedDate;

    // Approval
    private Long approvedBy;
    private String approvedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;
    
    private String approvalNotes;

    // Rejection
    private String rejectionReason;
    private Long rejectedBy;
    private String rejectedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rejectedAt;

    // Disbursement
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate disbursementDate;
    
    private String disbursementMethod;
    private String transactionReference;
    private Long disbursedBy;
    private String disbursedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime disbursedAt;

    private String notes;
    private String repaymentSchedule;
    private String metadata;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;

    // Computed fields
    private Boolean canBeModified;
    private BigDecimal repaymentProgress; // Percentage
}
