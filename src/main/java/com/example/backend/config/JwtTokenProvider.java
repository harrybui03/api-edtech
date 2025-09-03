package com.example.backend.config;

import com.example.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationInMs;

    @Value("${app.jwt-refresh-expiration-milliseconds}")
    private long jwtRefreshTokenExpirationInMs;
    
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateVerificationToken(String email, long expirationMinutes) {
        Instant now = Instant.now();
        Date expirationDate = Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES));

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiryInstant = now.plusMillis(jwtExpirationInMs);
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRole().name())
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("roles", roles)
                .claim("type", user.getUserType())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryInstant))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiryInstant = now.plusMillis(jwtRefreshTokenExpirationInMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryInstant))
                .signWith(key)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();
    }

    public String getEmail(String token){
        return getClaims(token).getSubject();
    }

    public void validateToken(String token) throws BadRequestException {
        if (token == null || token.trim().isEmpty()){
            throw new BadRequestException("Invalid JWT token");
        }
        try{
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token.trim()); // This validates the token
        } catch (SignatureException ex){ // Catches issues related to the secret key
            throw new BadRequestException("Invalid JWT signature");
        } catch (MalformedJwtException ex){
            throw new BadRequestException("Invalid JWT token");
        } catch (ExpiredJwtException ex){
            throw new BadRequestException("Expired JWT token");
        }catch (UnsupportedJwtException ex){
            throw new BadRequestException("Unsupported JWT token");
        }catch (IllegalArgumentException ex){
            throw new BadRequestException("JWT claims string is empty or null");
        }
    }
}