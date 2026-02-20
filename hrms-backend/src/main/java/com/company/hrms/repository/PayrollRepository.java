package com.company.hrms.repository;

import com.company.hrms.entity.Payroll;
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
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long>, JpaSpecificationExecutor<Payroll> {

    // Trouver la paie d'un employé pour une période
    Optional<Payroll> findByEmployeeIdAndPayPeriod(Long employeeId, String payPeriod);

    // Toutes les paies d'un employé
    @Query("SELECT p FROM Payroll p WHERE p.employee.id = :employeeId " +
           "AND p.deleted = false ORDER BY p.payPeriod DESC")
    Page<Payroll> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    // Paies par période
    @Query("SELECT p FROM Payroll p WHERE p.payPeriod = :payPeriod " +
           "AND p.deleted = false")
    List<Payroll> findByPayPeriod(@Param("payPeriod") String payPeriod);

    // Paies par statut
    @Query("SELECT p FROM Payroll p WHERE p.status = :status " +
           "AND p.deleted = false ORDER BY p.payPeriod DESC")
    Page<Payroll> findByStatus(@Param("status") PayrollStatus status, Pageable pageable);

    // Paies en attente d'approbation
    @Query("SELECT p FROM Payroll p WHERE p.status = 'PENDING' " +
           "AND p.deleted = false ORDER BY p.createdAt ASC")
    List<Payroll> findPendingPayrolls();

    // Paies approuvées mais non payées
    @Query("SELECT p FROM Payroll p WHERE p.status = 'APPROVED' " +
           "AND p.deleted = false ORDER BY p.paymentDate ASC")
    List<Payroll> findApprovedButNotPaid();

    // Total paie pour une période
    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p " +
           "WHERE p.payPeriod = :payPeriod AND p.status = 'PAID' " +
           "AND p.deleted = false")
    BigDecimal getTotalPayrollForPeriod(@Param("payPeriod") String payPeriod);

    // Total paie pour un département dans une période
    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p " +
           "WHERE p.employee.department.id = :departmentId " +
           "AND p.payPeriod = :payPeriod AND p.status = 'PAID' " +
           "AND p.deleted = false")
    BigDecimal getTotalPayrollForDepartment(
        @Param("departmentId") Long departmentId,
        @Param("payPeriod") String payPeriod
    );

    // Statistiques de paie pour une période
    @Query(value = "SELECT " +
           "COUNT(*) as total_employees, " +
           "SUM(gross_salary) as total_gross, " +
           "SUM(total_deductions) as total_deductions, " +
           "SUM(net_salary) as total_net, " +
           "AVG(net_salary) as average_salary " +
           "FROM payrolls WHERE pay_period = :payPeriod " +
           "AND status = 'PAID' AND deleted = false",
           nativeQuery = true)
    Object getPayrollStatistics(@Param("payPeriod") String payPeriod);

    // Employés avec heures supplémentaires dans une période
    @Query("SELECT p FROM Payroll p WHERE p.overtimeHours > 0 " +
           "AND p.payPeriod = :payPeriod AND p.deleted = false")
    List<Payroll> findPayrollsWithOvertime(@Param("payPeriod") String payPeriod);

    // Historique de paie d'un employé
    @Query("SELECT p FROM Payroll p WHERE p.employee.id = :employeeId " +
           "AND p.status = 'PAID' AND p.deleted = false " +
           "ORDER BY p.payPeriod DESC")
    List<Payroll> findPaymentHistory(@Param("employeeId") Long employeeId);

    // Vérifier si la paie existe déjà pour un employé et une période
    boolean existsByEmployeeIdAndPayPeriodAndDeletedFalse(Long employeeId, String payPeriod);

    // Paies par département
    @Query("SELECT p FROM Payroll p WHERE p.employee.department.id = :departmentId " +
           "AND p.payPeriod = :payPeriod AND p.deleted = false")
    List<Payroll> findByDepartmentAndPeriod(
        @Param("departmentId") Long departmentId,
        @Param("payPeriod") String payPeriod
    );

    // Compte des paies par statut
    @Query("SELECT COUNT(p) FROM Payroll p WHERE p.status = :status " +
           "AND p.deleted = false")
    Long countByStatus(@Param("status") PayrollStatus status);

    // Salaire total payé à un employé dans l'année
    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p " +
           "WHERE p.employee.id = :employeeId " +
           "AND SUBSTRING(p.payPeriod, 1, 4) = :year " +
           "AND p.status = 'PAID' AND p.deleted = false")
    BigDecimal getTotalSalaryForYear(
        @Param("employeeId") Long employeeId,
        @Param("year") String year
    );
}
