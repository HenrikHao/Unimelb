package com.team69.itproject.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PublicUtils {

    @Value("${custom.sign-key}")
    private String key;   //client secret

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(key.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return null;
        }
    }
}
