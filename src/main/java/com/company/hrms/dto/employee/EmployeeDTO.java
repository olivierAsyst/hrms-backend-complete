package com.company.hrms.dto.employee;

import com.company.hrms.enums.EmployeeStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
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
public class EmployeeDTO {

    private Long id;

    @NotBlank(message = "Employee number is required")
    @Size(max = 20, message = "Employee number must not exceed 20 characters")
    private String employeeNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Middle name must not exceed 50 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;

    @Size(max = 50, message = "National ID must not exceed 50 characters")
    private String nationalId;

    @Size(max = 50, message = "Passport number must not exceed 50 characters")
    private String passportNumber;

    private String address;

    private Long departmentId;
    private String departmentName;

    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @Size(max = 50, message = "Job level must not exceed 50 characters")
    private String jobLevel;

    private Long managerId;
    private String managerName;

    @NotNull(message = "Status is required")
    private EmployeeStatus status;

    @NotNull(message = "Hire date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate terminationDate;

    @Size(max = 50, message = "Contract type must not exceed 50 characters")
    private String contractType;

    @Size(max = 50, message = "Employment type must not exceed 50 characters")
    private String employmentType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    private BigDecimal salary;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency;

    private String emergencyContact;
    private String bankDetails;
    private String[] skills;
    private String education;
    private String workHistory;
    private String documents;
    private String customFields;

    private Long userId;
    private String username;

    @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
    private String profilePictureUrl;

    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate probationEndDate;

    @Size(max = 200, message = "Work location must not exceed 200 characters")
    private String workLocation;

    @Size(max = 100, message = "Work schedule must not exceed 100 characters")
    private String workSchedule;

    private Boolean overtimeEligible;
    private Boolean remoteWorkEligible;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;

    // Computed fields
    private String fullName;
    private Integer yearsOfService;
}
