package com.company.hrms.repository;

import com.company.hrms.entity.SalaryAdvance;
import com.company.hrms.enums.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SalaryAdvanceRepository extends JpaRepository<SalaryAdvance, Long>, JpaSpecificationExecutor<SalaryAdvance> {

    // Avances d'un employé
    @Query("SELECT sa FROM SalaryAdvance sa WHERE sa.employee.id = :employeeId " +
           "AND sa.deleted = false ORDER BY sa.requestDate DESC")
    Page<SalaryAdvance> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    // Avances par statut
    @Query("SELECT sa FROM SalaryAdvance sa WHERE sa.status = :status " +
           "AND sa.deleted = false ORDER BY sa.requestDate DESC")
    Page<SalaryAdvance> findByStatus(@Param("status") PayrollStatus status, Pageable pageable);

    // Avances en attente
    @Query("SELECT sa FROM SalaryAdvance sa WHERE sa.status = 'PENDING' " +
           "AND sa.deleted = false ORDER BY sa.requestDate ASC")
    List<SalaryAdvance> findPendingAdvances();

    // Avances actives (approuvées mais pas complètement remboursées)
    @Query("SELECT sa FROM SalaryAdvance sa WHERE sa.status IN ('APPROVED', 'PAID') " +
           "AND sa.fullyRepaid = false AND sa.deleted = false")
    List<SalaryAdvance> findActiveAdvances();

    // Avances actives d'un employé
    @Query("SELECT sa FROM SalaryAdvance sa WHERE sa.employee.id = :employeeId " +
           "AND sa.status IN ('APPROVED', 'PAID') " +
           "AND sa.fullyRepaid = false AND sa.deleted = false")
    List<SalaryAdvance> findActiveAdvancesByEmployee(@Param("employeeId") Long employeeId);

    // Total des avances actives d'un employé
    @Query("SELECT COALESCE(SUM(sa.remainingBalance), 0) FROM SalaryAdvance sa " +
           "WHERE sa.employee.id = :employeeId " +
           "AND sa.status IN ('APPROVED', 'PAID') " +
           "AND sa.fullyRepaid = false AND sa.deleted = false")
    BigDecimal getTotalActiveAdvances(@Param("employeeId") Long employeeId);

    // Remboursement mensuel total pour un employé
    @Query("SELECT COALESCE(SUM(sa.monthlyRepayment), 0) FROM SalaryAdvance sa " +
           "WHERE sa.employee.id = :employeeId " +
           "AND sa.status IN ('APPROVED', 'PAID') " +
           "AND sa.fullyRepaid = false AND sa.deleted = false")
    BigDecimal getMonthlyRepaymentAmount(@Param("employeeId") Long employeeId);

    // Avances approuvées en attente de décaissement
    @Query("SELECT sa FROM SalaryAdvance sa WHERE sa.status = 'APPROVED' " +
           "AND sa.amountDisbursed IS NULL AND sa.deleted = false")
    List<SalaryAdvance> findApprovedAwaitingDisbursement();

    // Historique complet des avances d'un employé
    @Query("SELECT sa FROM SalaryAdvance sa WHERE sa.employee.id = :employeeId " +
           "AND sa.deleted = false ORDER BY sa.requestDate DESC")
    List<SalaryAdvance> findAdvanceHistory(@Param("employeeId") Long employeeId);

    // Statistiques des avances pour une période
    @Query(value = "SELECT " +
           "COUNT(*) as total_requests, " +
           "COUNT(CASE WHEN status = 'APPROVED' OR status = 'PAID' THEN 1 END) as approved_count, " +
           "SUM(CASE WHEN status = 'APPROVED' OR status = 'PAID' THEN amount_approved ELSE 0 END) as total_approved, " +
           "SUM(CASE WHEN status = 'PAID' THEN amount_disbursed ELSE 0 END) as total_disbursed, " +
           "SUM(amount_repaid) as total_repaid " +
           "FROM salary_advances " +
           "WHERE request_date BETWEEN :startDate AND :endDate " +
           "AND deleted = false",
           nativeQuery = true)
    Object getAdvanceStatistics(
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate
    );

    // Vérifier si un employé a déjà une avance en attente ou active
    @Query("SELECT COUNT(sa) > 0 FROM SalaryAdvance sa WHERE sa.employee.id = :employeeId " +
           "AND sa.status IN ('PENDING', 'APPROVED', 'PAID') " +
           "AND (sa.status = 'PENDING' OR sa.fullyRepaid = false) " +
           "AND sa.deleted = false")
    boolean hasActivePendingAdvance(@Param("employeeId") Long employeeId);

    // Avances à rembourser ce mois
    @Query("SELECT sa FROM SalaryAdvance sa WHERE sa.fullyRepaid = false " +
           "AND sa.status = 'PAID' " +
           "AND sa.repaymentStartDate <= :currentDate " +
           "AND sa.repaymentEndDate >= :currentDate " +
           "AND sa.deleted = false")
    List<SalaryAdvance> findAdvancesForRepayment(@Param("currentDate") java.time.LocalDate currentDate);

    // Total avances décaissées dans une période
    @Query("SELECT COALESCE(SUM(sa.amountDisbursed), 0) FROM SalaryAdvance sa " +
           "WHERE sa.disbursementDate BETWEEN :startDate AND :endDate " +
           "AND sa.deleted = false")
    BigDecimal getTotalDisbursedInPeriod(
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate
    );
}
