package com.company.hrms.dto.absence;

import com.company.hrms.enums.AbsenceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDTO {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    private String employeeName;
    private String employeeNumber;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Leave type is required")
    private AbsenceType leaveType;

    private Integer totalAllocated;
    private Integer used;
    private Integer pending;
    private Integer available;
    private Integer carriedForward;
    private Integer carryForwardLimit;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiresOn;

    private String notes;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;

    // Computed fields
    private Integer remainingBalance;
    private Integer totalUsedAndPending;
}
