package com.company.hrms.entity;

import com.company.hrms.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
//import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_number", columnList = "employee_number"),
    @Index(name = "idx_employee_email", columnList = "email"),
    @Index(name = "idx_employee_department", columnList = "department_id"),
    @Index(name = "idx_employee_status", columnList = "status"),
    @Index(name = "idx_employee_hire_date", columnList = "hire_date"),
    @Index(name = "idx_employee_full_name", columnList = "first_name, last_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Column(name = "employee_number", nullable = false, unique = true, length = 20)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "national_id", length = 50)
    private String nationalId;

    @Column(name = "passport_number", length = 50)
    private String passportNumber;

    // Address stored as JSONB in PostgreSQL
    @Column(name = "address", columnDefinition = "jsonb")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(name = "job_level", length = 50)
    private String jobLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "contract_type", length = 50)
    private String contractType;

    @Column(name = "employment_type", length = 50)
    private String employmentType; // Full-time, Part-time, Contract, Intern

    @Column(name = "salary", precision = 15, scale = 2)
    private BigDecimal salary;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "USD";

    // Emergency contact stored as JSONB
    @Column(name = "emergency_contact", columnDefinition = "jsonb")
    private String emergencyContact;

    // Bank details stored as JSONB (encrypted in real scenario)
    @Column(name = "bank_details", columnDefinition = "jsonb")
    private String bankDetails;

    // Skills array stored in PostgreSQL
    @Column(name = "skills", columnDefinition = "text[]")
    private String[] skills;

    // Education history stored as JSONB
    @Column(name = "education", columnDefinition = "jsonb")
    private String education;

    // Work history stored as JSONB
    @Column(name = "work_history", columnDefinition = "jsonb")
    private String workHistory;

    // Documents (IDs, certificates, etc.) stored as JSONB
    @Column(name = "documents", columnDefinition = "jsonb")
    private String documents;

    // Custom fields for extensibility (stored as JSONB)
    @Column(name = "custom_fields", columnDefinition = "jsonb")
    private String customFields;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "work_location", length = 200)
    private String workLocation;

    @Column(name = "work_schedule", length = 100)
    private String workSchedule;

    @Column(name = "overtime_eligible", nullable = false)
    @Builder.Default
    private Boolean overtimeEligible = false;

    @Column(name = "remote_work_eligible", nullable = false)
    @Builder.Default
    private Boolean remoteWorkEligible = false;

    // PostgreSQL Full-Text Search
    @Column(name = "search_vector", columnDefinition = "tsvector")
    private String searchVector;

    // Computed full name for easier queries
    @Transient
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        fullName.append(" ").append(lastName);
        return fullName.toString();
    }

    @Transient
    public Integer getYearsOfService() {
        if (hireDate == null) return 0;
        LocalDate endDate = (terminationDate != null) ? terminationDate : LocalDate.now();
        return endDate.getYear() - hireDate.getYear();
    }
}
