package com.company.hrms.repository;

import com.company.hrms.entity.Role;
import com.company.hrms.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);

    Optional<Role> findByNameAndDeletedFalse(RoleType name);

    boolean existsByName(RoleType name);

    @Query("SELECT r FROM Role r WHERE r.deleted = false")
    List<Role> findAllActive();
}
