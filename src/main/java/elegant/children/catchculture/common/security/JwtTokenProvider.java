package elegant.children.catchculture.common.security;

import elegant.children.catchculture.common.utils.CookieUtils;
import elegant.children.catchculture.entity.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    public  static final String PREFIX = "Bearer ";
    private static final String EMAIL = "email";
    private static final String ROLE = "role";

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.token.expiration}")
    private int tokenExpiration;

    @Value("${jwt.token.header}")
    private String header;

    public void sendJwtTokenCookie(HttpServletResponse response, final String token) {
        final String cookieValue = PREFIX + token;
        CookieUtils.addCookie(response, header, cookieValue, tokenExpiration);}

    public String generateToken(final String email, final Role role) {
        return Jwts.builder()
                .setSubject("AccessToken")
                .claim(EMAIL, email)
                .claim(ROLE, role)
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration* 1000L))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String getEmail(final String token) {
        final Claims claims = parseClaims(token);
        return claims.get(EMAIL, String.class);
    }

    public Role getRole(final String token) {
        final Claims claims = parseClaims(token);
        return Role.valueOf(claims.get(ROLE, String.class));
    }

    public String resolveToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    public boolean validateToken(final String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (Exception e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }

        return false;
    }

    private Claims parseClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }




}
