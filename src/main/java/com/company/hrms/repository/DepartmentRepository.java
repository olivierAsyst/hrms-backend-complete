package com.company.hrms.repository;

import com.company.hrms.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    Optional<Department> findByCode(String code);

    Optional<Department> findByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT d FROM Department d WHERE d.deleted = false AND d.active = true")
    List<Department> findAllActive();

    @Query("SELECT d FROM Department d WHERE d.deleted = false")
    Page<Department> findAllNotDeleted(Pageable pageable);

    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) AND d.deleted = false")
    Page<Department> searchByName(@Param("name") String name, Pageable pageable);

    @Modifying
    @Query("UPDATE Department d SET d.deleted = true, d.deletedAt = :deletedAt, d.deletedBy = :deletedBy WHERE d.id = :departmentId")
    void softDelete(@Param("departmentId") Long departmentId, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") String deletedBy);
}
