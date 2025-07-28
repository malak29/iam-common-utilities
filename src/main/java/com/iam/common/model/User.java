// iam-common-utilities/src/main/java/com/iam/common/entity/User.java
package com.iam.common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"users\"")  // Quotes needed because 'user' is PostgreSQL reserved word
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "userid")
    private UUID userId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "hashedpassword", length = 255)
    private String hashedPassword;

    @Column(name = "orgid", nullable = false)
    private Integer orgId;

    @Column(name = "departmentid", nullable = false)
    private Integer departmentId;

    @Column(name = "usertypeid", nullable = false)
    private Integer userTypeId;

    @Column(name = "authtypeid", nullable = false)
    private Integer authTypeId;

    @Column(name = "userstatusid", nullable = false)
    private Integer userStatusId;

    @CreationTimestamp
    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    // Authentication-specific fields
    @Column(name = "lastlogin")
    private LocalDateTime lastLogin;

    @Column(name = "failedloginattempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "accountlocked")
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(name = "passwordresettoken", length = 255)
    private String passwordResetToken;

    @Column(name = "passwordresettokenexpiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Column(name = "emailverified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "emailverificationtoken", length = 255)
    private String emailVerificationToken;

    // Convenience methods
    public boolean isAccountNonLocked() {
        return !Boolean.TRUE.equals(accountLocked);
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void lockAccount() {
        this.accountLocked = true;
    }

    public void unlockAccount() {
        this.accountLocked = false;
        this.resetFailedLoginAttempts();
    }
}