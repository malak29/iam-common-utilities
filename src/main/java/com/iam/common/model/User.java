package com.iam.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column("userid")
    private UUID userId;

    @Column("email")
    private String email;

    @Column("username")
    private String username;

    @Column("name")
    private String name;

    @Column("hashedpassword")
    private String hashedPassword;

    @Column("orgid")
    private Integer orgId;

    @Column("departmentid")
    private Integer departmentId;

    @Column("usertypeid")
    private Integer userTypeId;

    @Column("authtypeid")
    private Integer authTypeId;

    @Column("userstatusid")
    private Integer userStatusId;

    @CreatedDate
    @Column("createdat")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updatedat")
    private LocalDateTime updatedAt;

    @Column("lastlogin")
    private LocalDateTime lastLogin;

    @Column("failedloginattempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column("accountlocked")
    @Builder.Default
    private Boolean accountLocked = false;

    @Column("passwordresettoken")
    private String passwordResetToken;

    @Column("passwordresettokenexpiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Column("emailverified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column("emailverificationtoken")
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