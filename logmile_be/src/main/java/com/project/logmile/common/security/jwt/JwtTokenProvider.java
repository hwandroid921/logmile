package com.project.logmile.common.security.jwt;

import com.project.logmile.common.enums.AdminRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secretValue;

	@Value("${jwt.expiration-ms:86400000}")
	private long expirationMs;

	private SecretKey secretKey;

	@PostConstruct
	void init() {
		this.secretKey = Keys.hmacShaKeyFor(secretValue.getBytes(StandardCharsets.UTF_8));
	}

	public String createToken(Long adminId, String email, AdminRole role) {
		Date now    = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);

		return Jwts.builder()
			.subject(String.valueOf(adminId))
			.claim("email", email)
			.claim("role",  role.name())
			.issuedAt(now)
			.expiration(expiry)
			.signWith(secretKey)
			.compact();
	}

	public Claims getClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public Long getAdminId(String token) {
		return Long.parseLong(getClaims(token).getSubject());
	}

	public AdminRole getRole(String token) {
		return AdminRole.valueOf(getClaims(token).get("role", String.class));
	}

	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			log.debug("만료된 JWT 토큰: {}", e.getMessage());
		} catch (JwtException | IllegalArgumentException e) {
			log.debug("유효하지 않은 JWT 토큰: {}", e.getMessage());
		}
		return false;
	}
}
