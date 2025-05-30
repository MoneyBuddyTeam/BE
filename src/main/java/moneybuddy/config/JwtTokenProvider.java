package moneybuddy.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24; // 24시간

    public String createToken(Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
            .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(Jwts.parser().setSigningKey(secretKey.getBytes())
            .parseClaimsJws(token)
            .getBody()
            .getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
