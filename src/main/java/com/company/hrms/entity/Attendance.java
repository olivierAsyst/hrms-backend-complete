package com.company.hrms.entity;

import com.company.hrms.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendances", indexes = {
    @Index(name = "idx_attendance_employee", columnList = "employee_id"),
    @Index(name = "idx_attendance_date", columnList = "attendance_date"),
    @Index(name = "idx_attendance_status", columnList = "status"),
    @Index(name = "idx_attendance_employee_date", columnList = "employee_id, attendance_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(name = "working_hours", precision = 5, scale = 2)
    private java.math.BigDecimal workingHours;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    @Builder.Default
    private java.math.BigDecimal overtimeHours = java.math.BigDecimal.ZERO;

    @Column(name = "late_minutes")
    @Builder.Default
    private Integer lateMinutes = 0;

    @Column(name = "early_departure_minutes")
    @Builder.Default
    private Integer earlyDepartureMinutes = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "absence_id")
    private Absence absence; // Lien vers l'absence si applicable

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "location", length = 200)
    private String location; // Lieu de travail

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IP du pointage

    @Column(name = "device_info", length = 200)
    private String deviceInfo; // Info appareil

    // Metadata stocké en JSONB
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    private Boolean verified = false; // Vérifié par le manager

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private java.time.LocalDateTime verifiedAt;

    // Méthodes utilitaires
    @Transient
    public java.math.BigDecimal getTotalHours() {
        if (workingHours == null) return java.math.BigDecimal.ZERO;
        if (overtimeHours == null) return workingHours;
        return workingHours.add(overtimeHours);
    }

    @Transient
    public boolean isLate() {
        return lateMinutes != null && lateMinutes > 0;
    }

    @Transient
    public boolean hasOvertime() {
        return overtimeHours != null && overtimeHours.compareTo(java.math.BigDecimal.ZERO) > 0;
    }
}
