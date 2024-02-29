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

    public String generateAuthToken(String username) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.HOUR, 1);
        return Jwts.builder().subject(username)
                .expiration(date.getTime()).signWith(getSecretKey()).compact();
    }

    @SuppressWarnings("unused")
    public String getUsernameFromToken(String token) {
        if (!isTokenValid(token)) return null;
        var jwt = Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
        return jwt.getPayload().getSubject();
    }

    private boolean isTokenValid(String token) {
        try {
            var jwt = Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
            return !isTokenExpired(jwt.getPayload());
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isTokenExpired(Claims token) {
        System.out.println(Calendar.getInstance().getTime());
        System.out.println(token.getExpiration());
        return !Calendar.getInstance().getTime().before(token.getExpiration());
    }

    private SecretKey getSecretKey() {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }
}
