package com.company.hrms.repository;

import com.company.hrms.entity.Attendance;
import com.company.hrms.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    // Trouver par employé et date
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    // Trouver les présences d'un employé pour une période
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.deleted = false ORDER BY a.attendanceDate DESC")
    List<Attendance> findByEmployeeAndDateRange(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Trouver par statut
    @Query("SELECT a FROM Attendance a WHERE a.status = :status " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.deleted = false")
    Page<Attendance> findByStatusAndDateRange(
        @Param("status") AttendanceStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    // Présences non vérifiées
    @Query("SELECT a FROM Attendance a WHERE a.verified = false " +
           "AND a.deleted = false ORDER BY a.attendanceDate DESC")
    Page<Attendance> findUnverifiedAttendances(Pageable pageable);

    // Statistiques de présence pour un employé
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.status = :status AND a.deleted = false")
    Long countByEmployeeAndStatusInDateRange(
        @Param("employeeId") Long employeeId,
        @Param("status") AttendanceStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Présences avec retards
    @Query("SELECT a FROM Attendance a WHERE a.lateMinutes > 0 " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.deleted = false")
    List<Attendance> findLateAttendances(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Présences avec heures supplémentaires
    @Query("SELECT a FROM Attendance a WHERE a.overtimeHours > 0 " +
           "AND a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.deleted = false")
    List<Attendance> findOvertimeAttendances(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Total heures supplémentaires pour un employé dans une période
    @Query("SELECT COALESCE(SUM(a.overtimeHours), 0) FROM Attendance a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.deleted = false")
    java.math.BigDecimal getTotalOvertimeHours(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Total heures de retard pour un employé dans une période
    @Query("SELECT COALESCE(SUM(a.lateMinutes), 0) FROM Attendance a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.deleted = false")
    Long getTotalLateMinutes(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Rapport de présence pour un département
    @Query("SELECT a FROM Attendance a WHERE a.employee.department.id = :departmentId " +
           "AND a.attendanceDate = :date AND a.deleted = false")
    List<Attendance> findByDepartmentAndDate(
        @Param("departmentId") Long departmentId,
        @Param("date") LocalDate date
    );

    // Taux de présence global pour une date
    @Query(value = "SELECT " +
           "COUNT(CASE WHEN status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(*) as attendance_rate " +
           "FROM attendances WHERE attendance_date = :date AND deleted = false",
           nativeQuery = true)
    Double getAttendanceRateForDate(@Param("date") LocalDate date);

    // Employés absents aujourd'hui
    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate = :date " +
           "AND a.status IN ('ABSENT', 'ON_LEAVE') AND a.deleted = false")
    List<Attendance> findAbsentEmployeesForDate(@Param("date") LocalDate date);
}
