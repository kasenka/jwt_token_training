package org.example.jwt_tokens_training.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.example.jwt_tokens_training.dto.UserLoginDTO;
import org.example.jwt_tokens_training.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

@Service
public class JwtService {
    private final String jwtSecret;
    private final long jwtExpiration;

    public JwtService(
            @Value("classpath:${jwt.secret}") Resource secretResource,
            @Value("${jwt.expiration}") long jwtExpiration
    ) throws IOException {
        this.jwtSecret = new String(secretResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(UserLoginDTO user){
        return Jwts.builder()
                .setSubject(user.getUsername())
//                .claim("roles", user.getRoles())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }

}
