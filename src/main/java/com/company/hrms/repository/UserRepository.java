package com.company.hrms.repository;

import com.company.hrms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameAndDeletedFalse(String username);

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    java.util.List<User> findAllActive();

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = :locked, u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId, @Param("locked") Boolean locked, @Param("lockedUntil") LocalDateTime lockedUntil);

    @Modifying
    @Query("UPDATE User u SET u.deleted = true, u.deletedAt = :deletedAt, u.deletedBy = :deletedBy WHERE u.id = :userId")
    void softDelete(@Param("userId") Long userId, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") String deletedBy);
}
