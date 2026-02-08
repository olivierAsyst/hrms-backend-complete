package com.company.hrms.repository;

import com.company.hrms.entity.Absence;
import com.company.hrms.enums.AbsenceStatus;
import com.company.hrms.enums.AbsenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long>, JpaSpecificationExecutor<Absence> {

    // Absences d'un employé
    @Query("SELECT a FROM Absence a WHERE a.employee.id = :employeeId " +
           "AND a.deleted = false ORDER BY a.startDate DESC")
    Page<Absence> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    // Absences par statut
    @Query("SELECT a FROM Absence a WHERE a.status = :status " +
           "AND a.deleted = false ORDER BY a.startDate DESC")
    Page<Absence> findByStatus(@Param("status") AbsenceStatus status, Pageable pageable);

    // Absences en attente d'approbation
    @Query("SELECT a FROM Absence a WHERE a.status = 'PENDING' " +
           "AND a.deleted = false ORDER BY a.createdAt ASC")
    List<Absence> findPendingAbsences();

    // Absences d'un employé pour une période
    @Query("SELECT a FROM Absence a WHERE a.employee.id = :employeeId " +
           "AND ((a.startDate BETWEEN :startDate AND :endDate) " +
           "OR (a.endDate BETWEEN :startDate AND :endDate) " +
           "OR (a.startDate <= :startDate AND a.endDate >= :endDate)) " +
           "AND a.deleted = false")
    List<Absence> findByEmployeeAndDateRange(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Vérifier les conflits de dates pour un employé
    @Query("SELECT COUNT(a) > 0 FROM Absence a WHERE a.employee.id = :employeeId " +
           "AND a.status IN ('PENDING', 'APPROVED') " +
           "AND ((a.startDate BETWEEN :startDate AND :endDate) " +
           "OR (a.endDate BETWEEN :startDate AND :endDate) " +
           "OR (a.startDate <= :startDate AND a.endDate >= :endDate)) " +
           "AND (:absenceId IS NULL OR a.id != :absenceId) " +
           "AND a.deleted = false")
    boolean hasConflictingAbsence(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("absenceId") Long absenceId
    );

    // Absences par type
    @Query("SELECT a FROM Absence a WHERE a.type = :type " +
           "AND a.employee.id = :employeeId " +
           "AND a.deleted = false ORDER BY a.startDate DESC")
    List<Absence> findByEmployeeAndType(
        @Param("employeeId") Long employeeId,
        @Param("type") AbsenceType type
    );

    // Statistiques: Total jours d'absence par type pour un employé
    @Query("SELECT COALESCE(SUM(a.totalDays), 0) FROM Absence a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.type = :type " +
           "AND a.status = 'APPROVED' " +
           "AND EXTRACT(YEAR FROM a.startDate) = :year " +
           "AND a.deleted = false")
    Integer getTotalDaysByTypeAndYear(
        @Param("employeeId") Long employeeId,
        @Param("type") AbsenceType type,
        @Param("year") Integer year
    );

    // Absences à venir
    @Query("SELECT a FROM Absence a WHERE a.startDate > :currentDate " +
           "AND a.status = 'APPROVED' AND a.deleted = false " +
           "ORDER BY a.startDate ASC")
    List<Absence> findUpcomingAbsences(@Param("currentDate") LocalDate currentDate);

    // Employés en congé aujourd'hui
    @Query("SELECT a FROM Absence a WHERE :currentDate BETWEEN a.startDate AND a.endDate " +
           "AND a.status = 'APPROVED' AND a.deleted = false")
    List<Absence> findCurrentAbsences(@Param("currentDate") LocalDate currentDate);

    // Absences nécessitant un remplaçant
    @Query("SELECT a FROM Absence a WHERE a.substituteEmployee IS NULL " +
           "AND a.status = 'APPROVED' " +
           "AND a.startDate >= :currentDate " +
           "AND a.deleted = false")
    List<Absence> findAbsencesNeedingSubstitute(@Param("currentDate") LocalDate currentDate);

    // Rapport d'absences pour un département
    @Query("SELECT a FROM Absence a WHERE a.employee.department.id = :departmentId " +
           "AND a.startDate BETWEEN :startDate AND :endDate " +
           "AND a.deleted = false")
    List<Absence> findByDepartmentAndDateRange(
        @Param("departmentId") Long departmentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Compte des absences par statut pour un employé
    @Query("SELECT COUNT(a) FROM Absence a WHERE a.employee.id = :employeeId " +
           "AND a.status = :status AND a.deleted = false")
    Long countByEmployeeAndStatus(
        @Param("employeeId") Long employeeId,
        @Param("status") AbsenceStatus status
    );
}
