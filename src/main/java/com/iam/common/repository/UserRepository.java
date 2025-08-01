package com.iam.common.repository;

import com.iam.common.model.User;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    // Basic lookups
    Mono<Boolean> existsByEmail(String email);
    Mono<Boolean> existsByUsername(String username);
    Mono<User> findByEmail(String email);
    Mono<User> findByUsername(String username);

    @Query("SELECT * FROM users WHERE email = :email OR username = :email")
    Mono<User> findByEmailOrUsername(String email);

    // Status-based queries
    Flux<User> findByUserStatusId(Integer statusId);
    Mono<User> findByEmailAndUserStatusId(String email, Integer statusId);

    // Organization queries
    Flux<User> findByOrgId(Integer orgId);
    Flux<User> findByDepartmentId(Integer departmentId);
    Flux<User> findByOrgIdAndDepartmentId(Integer orgId, Integer departmentId);

    // Authentication queries
    @Query("SELECT * FROM users WHERE passwordresettoken = :token AND passwordresettokenexpiry > :now")
    Mono<User> findByValidPasswordResetToken(String token, LocalDateTime now);

    Mono<User> findByEmailVerificationToken(String token);
    Flux<User> findByAccountLocked(Boolean locked);

    // Update operations
    @Modifying
    @Query("UPDATE users SET lastlogin = :loginTime WHERE userid = :userId")
    Mono<Integer> updateLastLogin(UUID userId, LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE users SET failedloginattempts = :attempts WHERE userid = :userId")
    Mono<Integer> updateFailedLoginAttempts(UUID userId, Integer attempts);

    @Modifying
    @Query("UPDATE users SET accountlocked = :locked WHERE userid = :userId")
    Mono<Integer> updateAccountLocked(UUID userId, Boolean locked);
}