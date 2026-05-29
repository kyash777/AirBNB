package com.yash.projects.airBnb.security;
import com.yash.projects.airBnb.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class JWTService {
    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(this.jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder().subject(user.getId().toString()).claim("email", user.getEmail()).claim("roles", user.getRoles().toString()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 600000L)).signWith(this.getSecretKey()).compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder().subject(user.getId().toString()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 15552000000L)).signWith(this.getSecretKey()).compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = (Claims)Jwts.parser().verifyWith(this.getSecretKey()).build().parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }
}