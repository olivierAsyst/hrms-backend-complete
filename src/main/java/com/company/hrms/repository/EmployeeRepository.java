package com.company.hrms.repository;

import com.company.hrms.entity.Employee;
import com.company.hrms.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeNumberAndDeletedFalse(String employeeNumber);

    Optional<Employee> findByEmailAndDeletedFalse(String email);

    boolean existsByEmployeeNumber(String employeeNumber);

    boolean existsByEmail(String email);

    boolean existsByEmployeeNumberAndIdNot(String employeeNumber, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Find active employees
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.status = 'ACTIVE'")
    List<Employee> findAllActive();

    // Find by department
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false")
    Page<Employee> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);

    // Find by status
    @Query("SELECT e FROM Employee e WHERE e.status = :status AND e.deleted = false")
    Page<Employee> findByStatus(@Param("status") EmployeeStatus status, Pageable pageable);

    // Find by manager
    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId AND e.deleted = false")
    List<Employee> findByManagerId(@Param("managerId") Long managerId);

    // Full-text search using PostgreSQL tsvector
    @Query(value = "SELECT * FROM employees WHERE search_vector @@ plainto_tsquery('english', :searchTerm) AND deleted = false", 
           nativeQuery = true)
    Page<Employee> fullTextSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Advanced search with multiple criteria
    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND (:firstName IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) " +
           "AND (:lastName IS NULL OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) " +
           "AND (:email IS NULL OR LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Employee> advancedSearch(
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("email") String email,
        @Param("departmentId") Long departmentId,
        @Param("status") EmployeeStatus status,
        Pageable pageable
    );

    // Find employees hired in a date range
    @Query("SELECT e FROM Employee e WHERE e.hireDate BETWEEN :startDate AND :endDate AND e.deleted = false")
    List<Employee> findByHireDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find employees with birthdays in current month
    @Query(value = "SELECT * FROM employees WHERE EXTRACT(MONTH FROM date_of_birth) = EXTRACT(MONTH FROM CURRENT_DATE) " +
                   "AND deleted = false", nativeQuery = true)
    List<Employee> findEmployeesWithBirthdayThisMonth();

    // Find employees with upcoming probation end
    @Query("SELECT e FROM Employee e WHERE e.probationEndDate BETWEEN :startDate AND :endDate AND e.deleted = false")
    List<Employee> findEmployeesWithUpcomingProbationEnd(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Count employees by department
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false AND e.status = 'ACTIVE'")
    Long countByDepartmentId(@Param("departmentId") Long departmentId);

    // Count employees by status
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status AND e.deleted = false")
    Long countByStatus(@Param("status") EmployeeStatus status);

    // Find employees with specific skill (PostgreSQL array operation)
    @Query(value = "SELECT * FROM employees WHERE :skill = ANY(skills) AND deleted = false", nativeQuery = true)
    List<Employee> findBySkill(@Param("skill") String skill);

    // Soft delete
    @Modifying
    @Query("UPDATE Employee e SET e.deleted = true, e.deletedAt = :deletedAt, e.deletedBy = :deletedBy WHERE e.id = :employeeId")
    void softDelete(@Param("employeeId") Long employeeId, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") String deletedBy);

    // Update status
    @Modifying
    @Query("UPDATE Employee e SET e.status = :status WHERE e.id = :employeeId")
    void updateStatus(@Param("employeeId") Long employeeId, @Param("status") EmployeeStatus status);

    // Statistics queries using PostgreSQL aggregate functions
    @Query(value = "SELECT " +
                   "COUNT(*) as total, " +
                   "COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active, " +
                   "COUNT(CASE WHEN status = 'INACTIVE' THEN 1 END) as inactive, " +
                   "COUNT(CASE WHEN status = 'ON_LEAVE' THEN 1 END) as on_leave, " +
                   "COUNT(CASE WHEN status = 'TERMINATED' THEN 1 END) as terminated " +
                   "FROM employees WHERE deleted = false",
           nativeQuery = true)
    Object getEmployeeStatistics();
}
