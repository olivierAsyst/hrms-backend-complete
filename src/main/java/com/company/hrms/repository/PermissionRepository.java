package com.company.hrms.repository;

import com.company.hrms.entity.Permission;
import com.company.hrms.enums.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(PermissionType name);

    Optional<Permission> findByNameAndDeletedFalse(PermissionType name);

    boolean existsByName(PermissionType name);

    @Query("SELECT p FROM Permission p WHERE p.deleted = false")
    List<Permission> findAllActive();
}
