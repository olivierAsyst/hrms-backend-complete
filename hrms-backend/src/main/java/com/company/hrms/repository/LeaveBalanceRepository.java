package com.company.hrms.repository;

import com.company.hrms.entity.LeaveBalance;
import com.company.hrms.enums.AbsenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long>, JpaSpecificationExecutor<LeaveBalance> {

    // Trouver le solde pour un employé, année et type
    Optional<LeaveBalance> findByEmployeeIdAndYearAndLeaveType(
        Long employeeId,
        Integer year,
        AbsenceType leaveType
    );

    // Tous les soldes d'un employé pour une année
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId " +
           "AND lb.year = :year AND lb.deleted = false")
    List<LeaveBalance> findByEmployeeAndYear(
        @Param("employeeId") Long employeeId,
        @Param("year") Integer year
    );

    // Soldes avec congés disponibles
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId " +
           "AND lb.year = :year AND lb.available > 0 AND lb.deleted = false")
    List<LeaveBalance> findAvailableBalances(
        @Param("employeeId") Long employeeId,
        @Param("year") Integer year
    );

    // Soldes expirés ou proches de l'expiration
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.expiresOn IS NOT NULL " +
           "AND lb.expiresOn <= :date AND lb.available > 0 " +
           "AND lb.deleted = false")
    List<LeaveBalance> findExpiringBalances(@Param("date") java.time.LocalDate date);

    // Total congés disponibles pour un employé
    @Query("SELECT COALESCE(SUM(lb.available), 0) FROM LeaveBalance lb " +
           "WHERE lb.employee.id = :employeeId AND lb.year = :year " +
           "AND lb.deleted = false")
    Integer getTotalAvailableLeave(
        @Param("employeeId") Long employeeId,
        @Param("year") Integer year
    );

    // Vérifier si un employé a suffisamment de congés
    @Query("SELECT CASE WHEN lb.available >= :requiredDays THEN true ELSE false END " +
           "FROM LeaveBalance lb WHERE lb.employee.id = :employeeId " +
           "AND lb.year = :year AND lb.leaveType = :leaveType " +
           "AND lb.deleted = false")
    Boolean hasEnoughLeaveBalance(
        @Param("employeeId") Long employeeId,
        @Param("year") Integer year,
        @Param("leaveType") AbsenceType leaveType,
        @Param("requiredDays") Integer requiredDays
    );

    // Rapport de soldes pour tous les employés
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.year = :year " +
           "AND lb.deleted = false ORDER BY lb.employee.id")
    List<LeaveBalance> findAllByYear(@Param("year") Integer year);

    // Employés avec solde négatif (alerte)
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.available < 0 " +
           "AND lb.deleted = false")
    List<LeaveBalance> findNegativeBalances();
}
