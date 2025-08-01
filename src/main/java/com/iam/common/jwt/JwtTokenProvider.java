package com.iam.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:your-super-secret-jwt-key-that-should-be-at-least-256-bits}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Reactive token generation
    public Mono<String> generateToken(String username) {
        return Mono.fromCallable(() -> {
            Map<String, Object> claims = new HashMap<>();
            return createToken(claims, username);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> generateToken(String username, Map<String, Object> claims) {
        return Mono.fromCallable(() -> createToken(claims, username))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // Reactive token validation
    public Mono<Claims> validateToken(String token) {
        return Mono.fromCallable(() -> extractAllClaims(token))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex -> {
                    log.error("Token validation failed: {}", ex.getMessage());
                    return Mono.error(new RuntimeException("Invalid JWT token", ex));
                });
    }

    public Mono<String> extractUsername(String token) {
        return validateToken(token)
                .map(Claims::getSubject);
    }

    public Mono<Boolean> isTokenExpired(String token) {
        return validateToken(token)
                .map(claims -> claims.getExpiration().before(new Date()))
                .onErrorReturn(true);
    }

    public Mono<String> generateRefreshToken(String username) {
        return Mono.fromCallable(() -> {
            Map<String, Object> claims = new HashMap<>();
            claims.put("tokenType", "refresh");

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + (jwtExpirationMs * 7));

            return Jwts.builder()
                    .claims(claims)
                    .subject(username)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getSigningKey())
                    .compact();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // Keep blocking versions for backward compatibility
    public String generateTokenSync(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    public Boolean validateTokenSync(String token) {
        try {
            extractAllClaims(token);
            return !extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Private helper methods (unchanged)
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
}