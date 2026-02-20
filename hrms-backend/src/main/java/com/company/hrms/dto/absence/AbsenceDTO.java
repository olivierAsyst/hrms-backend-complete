package com.company.hrms.dto.absence;

import com.company.hrms.enums.AbsenceStatus;
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
public class AbsenceDTO {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    private String employeeName;
    private String employeeNumber;

    @NotNull(message = "Absence type is required")
    private AbsenceType type;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Integer totalDays;
    private Boolean isHalfDay;

    @NotNull(message = "Status is required")
    private AbsenceStatus status;

    private String reason;
    private String supportingDocumentUrl;

    private Long approvedBy;
    private String approvedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    private String rejectionReason;

    private Boolean isPaid;
    private Boolean deductedFromBalance;
    private Integer balanceBefore;
    private Integer balanceAfter;

    private Boolean isEmergency;
    private String emergencyContact;

    private Long substituteEmployeeId;
    private String substituteEmployeeName;
    private String substituteNotes;

    private String notes;
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
    private Long durationInDays;
}
