// iam-common-utilities/src/main/java/com/iam/common/jwt/JwtTokenProvider.java
package com.iam.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:your-super-secret-jwt-key-that-should-be-at-least-256-bits}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    // Generate secret key from string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generate token with username
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    // Generate token with custom claims
    public String generateToken(String username, Map<String, Object> claims) {
        return createToken(claims, username);
    }

    // Create token with claims and subject
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, getSigningKey())
                .compact();
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    // Check if token is expired
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    // Validate token against username
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Validate token (general validation)
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Get expiration time in milliseconds
    public long getExpirationTime() {
        return jwtExpirationMs;
    }

    // Get remaining expiration time for a token
    public long getRemainingExpirationTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Error getting remaining expiration time: {}", e.getMessage());
            return 0;
        }
    }

    // Extract custom claim
    public Object extractClaim(String token, String claimName) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get(claimName);
        } catch (Exception e) {
            log.error("Error extracting claim '{}': {}", claimName, e.getMessage());
            return null;
        }
    }

    // Check if token can be refreshed
    public Boolean canTokenBeRefreshed(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error checking if token can be refreshed: {}", e.getMessage());
            return false;
        }
    }

    // Generate refresh token (longer expiration)
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (jwtExpirationMs * 7)); // 7 times longer

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, getSigningKey())
                .compact();
    }

    // Check if token is a refresh token
    public Boolean isRefreshToken(String token) {
        try {
            Object tokenType = extractClaim(token, "tokenType");
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            log.error("Error checking if token is refresh token: {}", e.getMessage());
            return false;
        }
    }
}