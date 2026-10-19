package com.security_starter.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.security_starter.security.userdetails.CustomUserDetails;
import com.security_starter.user.entity.UserEntity;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {
	
	private static final String ACCESS_TOKEN_TYPE="access";
	private static final int MIN_SECRET_BYTES=32;
	
    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    
    @Value("${spring.jwt.issuer}")
    private String jwtIssuer;

    @PostConstruct
    void validateConfiguration() {
    	if(jwtSecret == null || jwtSecret.isBlank()) {
    		throw new IllegalStateException("JWT secret must be configured");
    	}
    	
    	if(jwtSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
    		throw new IllegalStateException("JWT secret must contain at least 32 UTF-8 bytes");
    	}
    	
        if (jwtIssuer == null || jwtIssuer.isBlank()) {
            throw new IllegalStateException("JWT issuer must be configured");
        }

        if (accessTokenExpiration <= 0) {
            throw new IllegalStateException("JWT access token expiration must be greater than zero");
        }
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("type", ACCESS_TOKEN_TYPE);

        return buildToken(claims, userDetails, accessTokenExpiration);
    }
    
    public String generateAccessToken(UserEntity user) {
        return generateAccessToken(new CustomUserDetails(user));
    }
    
    public long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000;
    }

    private String buildToken(
            Map<String, Object> claims,
            UserDetails userDetails,
            long expiration
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .issuer(jwtIssuer)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public String extractTokenId(String token) {
    	return extractClaim(token, Claims::getId);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type",String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
        		.requireIssuer(jwtIssuer)
        		.require("type", ACCESS_TOKEN_TYPE)
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);

        String tokenType = extractTokenType(token);
        
        return ACCESS_TOKEN_TYPE.equals(tokenType) 
        		&& username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}