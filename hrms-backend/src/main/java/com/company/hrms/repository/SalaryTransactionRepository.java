package com.company.hrms.repository;

import com.company.hrms.entity.SalaryTransaction;
import com.company.hrms.enums.SalaryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalaryTransactionRepository extends JpaRepository<SalaryTransaction, Long>, JpaSpecificationExecutor<SalaryTransaction> {

    // Transactions d'un employé
    @Query("SELECT st FROM SalaryTransaction st WHERE st.employee.id = :employeeId " +
           "AND st.deleted = false ORDER BY st.transactionDate DESC")
    Page<SalaryTransaction> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    // Transactions par type
    @Query("SELECT st FROM SalaryTransaction st WHERE st.transactionType = :type " +
           "AND st.deleted = false ORDER BY st.transactionDate DESC")
    Page<SalaryTransaction> findByType(@Param("type") SalaryTransactionType type, Pageable pageable);

    // Transactions d'un employé pour une période
    @Query("SELECT st FROM SalaryTransaction st WHERE st.employee.id = :employeeId " +
           "AND st.transactionDate BETWEEN :startDate AND :endDate " +
           "AND st.deleted = false ORDER BY st.transactionDate DESC")
    List<SalaryTransaction> findByEmployeeAndDateRange(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Transactions liées à une paie
    @Query("SELECT st FROM SalaryTransaction st WHERE st.payroll.id = :payrollId " +
           "AND st.deleted = false")
    List<SalaryTransaction> findByPayrollId(@Param("payrollId") Long payrollId);

    // Transactions liées à une avance
    @Query("SELECT st FROM SalaryTransaction st WHERE st.salaryAdvance.id = :advanceId " +
           "AND st.deleted = false")
    List<SalaryTransaction> findBySalaryAdvanceId(@Param("advanceId") Long advanceId);

    // Total crédits pour un employé
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SalaryTransaction st " +
           "WHERE st.employee.id = :employeeId " +
           "AND st.isCredit = true " +
           "AND st.transactionDate BETWEEN :startDate AND :endDate " +
           "AND st.deleted = false")
    BigDecimal getTotalCredits(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Total débits pour un employé
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SalaryTransaction st " +
           "WHERE st.employee.id = :employeeId " +
           "AND st.isCredit = false " +
           "AND st.transactionDate BETWEEN :startDate AND :endDate " +
           "AND st.deleted = false")
    BigDecimal getTotalDebits(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Balance nette
    @Query("SELECT COALESCE(SUM(CASE WHEN st.isCredit = true THEN st.amount ELSE -st.amount END), 0) " +
           "FROM SalaryTransaction st " +
           "WHERE st.employee.id = :employeeId " +
           "AND st.transactionDate BETWEEN :startDate AND :endDate " +
           "AND st.deleted = false")
    BigDecimal getNetBalance(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Statistiques par type de transaction
    @Query(value = "SELECT " +
           "transaction_type, " +
           "COUNT(*) as count, " +
           "SUM(amount) as total_amount " +
           "FROM salary_transactions " +
           "WHERE employee_id = :employeeId " +
           "AND transaction_date BETWEEN :startDate AND :endDate " +
           "AND deleted = false " +
           "GROUP BY transaction_type",
           nativeQuery = true)
    List<Object[]> getTransactionStatistics(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Dernières transactions
    @Query("SELECT st FROM SalaryTransaction st WHERE st.employee.id = :employeeId " +
           "AND st.deleted = false ORDER BY st.transactionDate DESC, st.createdAt DESC")
    Page<SalaryTransaction> findRecentTransactions(@Param("employeeId") Long employeeId, Pageable pageable);

    // Transactions en attente de traitement
    @Query("SELECT st FROM SalaryTransaction st WHERE st.processedAt IS NULL " +
           "AND st.deleted = false ORDER BY st.transactionDate ASC")
    List<SalaryTransaction> findPendingTransactions();
}
