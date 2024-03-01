package ru.prodcontest.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    public String generateAuthToken(String username, String password) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.HOUR, 1);
        return Jwts.builder().subject(username)
                .claim("password", password)
                .expiration(date.getTime()).signWith(getSecretKey()).compact();
    }

    public String getUsernameFromToken(String token) {
        if (isTokenInvalid(token)) return null;
        var jwt = Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
        return jwt.getPayload().getSubject();
    }

    public String getPasswordFromToken(String token) {
        if (isTokenInvalid(token)) return null;
        var jwt = Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
        return (String) jwt.getPayload().get("password");
    }

    private boolean isTokenInvalid(String token) {
        try {
            var jwt = Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
            return isTokenExpired(jwt.getPayload());
        } catch (JwtException e) {
            return true;
        }
    }

    private boolean isTokenExpired(Claims token) {
        return !Calendar.getInstance().getTime().before(token.getExpiration());
    }

    private SecretKey getSecretKey() {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }
}
