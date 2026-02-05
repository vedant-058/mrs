package com.mrs.mrs.service;

import com.mrs.mrs.exception.AuthenticationException;
import com.mrs.mrs.model.User.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

@Service
public class TokenService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;
    private final Set<String> blacklistedTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private SecretKey secretKey;

    private SecretKey getSigningKey() {
        if (this.secretKey == null) {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return this.secretKey;
    }
    
    // /**
    //  * Generates a new JWT using modern JJWT builder methods.
    //  */
    public String generateToken(UUID userId, UserRole role) {
        
        Instant now = Instant.now();
        Instant expiryInstant = now.plusMillis(jwtExpirationMs);

        return Jwts.builder()
                .claim("userId", userId.toString()) 
                .claim("role", role.name()) 
                .issuedAt(Date.from(now)) 
                .expiration(Date.from(expiryInstant)) 
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // /**
    //  * Parses and extracts the Claims (payload) from a signed token.
    //  * Uses the modern Jwts.parser().verifyWith() method.
    //  */
    public Claims parseTokenClaims(String token) {
        // Now this method works because getSigningKey() returns SecretKey
        return Jwts.parser()
                .verifyWith(getSigningKey()) 
                .build()
                .parseSignedClaims(token) 
                .getPayload(); 
    }

    /**
     * Validates the token's signature, integrity, and expiration.
     * Also checks if the token is blacklisted.
    */
    public boolean validateToken(String authToken) {
        // Check if token is blacklisted first
        if (isTokenBlacklisted(authToken)) {
            return false;
        }
        
        try {
            // Attempt to parse the claims, which performs all validation checks
            parseTokenClaims(authToken);
            return true; 
        } catch (ExpiredJwtException e) {
            // JWT token is expired
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (MalformedJwtException e) {
            // Invalid JWT token format (e.g., corrupted structure)
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (SignatureException e) {
            // Signature verification failed (e.g., secret key mismatch, token tampered)
            System.err.println("Invalid JWT signature: " + e.getMessage());
        } catch (JwtException e) {
            // Other JWT-related exceptions
            System.err.println("JWT exception: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // JWT string is empty or null
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Helper to get the User ID from a valid token.
     * Note: This method assumes the token has already been validated.
     * It will throw AuthenticationException if the token is invalid or expired.
    */
    public UUID getUserIdFromToken(String token) {
        // Check if token is blacklisted
        if (isTokenBlacklisted(token)) {
            throw new AuthenticationException("Token has been invalidated");
        }
        
        try {
            Claims claims = parseTokenClaims(token);
            String userIdStr = claims.get("userId", String.class);
            
            if (userIdStr == null) {
                throw new AuthenticationException("User ID not found in token");
            }
            
            return UUID.fromString(userIdStr);
    
        } catch (ExpiredJwtException ex) {
            // Token is valid but expired
            throw new AuthenticationException("Token has expired");
    
        } catch (MalformedJwtException ex) {
            // Invalid JWT token format
            throw new AuthenticationException("Invalid token format");
            
        } catch (SignatureException ex) {
            // Signature verification failed
            throw new AuthenticationException("Invalid token signature");
            
        } catch (JwtException ex) {
            // Other JWT-related exceptions
            throw new AuthenticationException("Invalid authentication token");
    
        } catch (IllegalArgumentException ex) {
            // Invalid UUID format
            throw new AuthenticationException("Invalid user ID in token");
        }
    }

    public void blacklistToken(String jwt) {
        blacklistedTokens.add(jwt);
    }

    public boolean isTokenBlacklisted(String jwt) {
        return blacklistedTokens.contains(jwt);
    }
}