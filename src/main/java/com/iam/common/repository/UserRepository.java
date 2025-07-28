// iam-common-utilities/src/main/java/com/iam/common/repository/UserRepository.java
package com.iam.common.repository;

import com.iam.common.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Basic lookups

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByUsername(String username);

    @Query("SELECT u FROM User u WHERE (u.email = :emailOrUsername OR u.username = :emailOrUsername)")
    Optional<User> findUserByEmailOrUsername(@Param("emailOrUsername") String emailOrUsername);

    // Status-based queries
    @Query("SELECT u FROM User u WHERE u.userStatusId = :statusId")
    List<User> findUserByUserStatusId(@Param("statusId") Integer statusId);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.userStatusId = :statusId")
    Optional<User> findUserByEmailAndStatus(@Param("email") String email, @Param("statusId") Integer statusId);

    // Organization and department queries
    @Query("SELECT u FROM User u WHERE u.orgId = :orgId")
    List<User> findUsersByOrgId(@Param("orgId") Integer orgId);

    @Query("SELECT u FROM User u WHERE u.departmentId = :departmentId")
    List<User> findUsersByDepartmentId(@Param("departmentId") Integer departmentId);

    @Query("SELECT u FROM User u WHERE u.orgId = :orgId AND u.departmentId = :departmentId")
    List<User> findUsersByOrgIdAndDepartmentId(@Param("orgId") Integer orgId, @Param("departmentId") Integer departmentId);

    // Authentication-specific queries
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetTokenExpiry > :now")
    Optional<User> findUserByValidPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
    Optional<User> findUserByEmailVerificationToken(@Param("token") String token);

    @Query("SELECT u FROM User u WHERE u.accountLocked = true")
    List<User> findUserLockedAccounts();

    // Update operations for authentication
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.userId = :userId")
    void updateUserLastLogin(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.userId = :userId")
    void updateUserFailedLoginAttempts(@Param("userId") UUID userId, @Param("attempts") Integer attempts);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.accountLocked = :locked WHERE u.userId = :userId")
    void updateUserAccountLocked(@Param("userId") UUID userId, @Param("locked") Boolean locked);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordResetToken = :token, u.passwordResetTokenExpiry = :expiry WHERE u.userId = :userId")
    void updateUserPasswordResetToken(@Param("userId") UUID userId, @Param("token") String token, @Param("expiry") LocalDateTime expiry);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.hashedPassword = :hashedPassword, u.passwordResetToken = null, u.passwordResetTokenExpiry = null WHERE u.userId = :userId")
    void updateUserPassword(@Param("userId") UUID userId, @Param("hashedPassword") String hashedPassword);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = true, u.emailVerificationToken = null WHERE u.userId = :userId")
    void verifyUserEmail(@Param("userId") UUID userId);
}