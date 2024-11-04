package shop.nuribooks.gateway.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

/**
 *  jwt Utils
 *  @author : yongtaek
 */
@Component
public class JwtUtils {
	private SecretKey secretKey;

	public JwtUtils(@Value("${spring.jwt.secret}") String secret) {
		this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	public String getUsername(String token) {
		return Jwts
			.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("username", String.class);
	}

	public String getRole(String token) {
		return Jwts
			.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("role", String.class);
	}

	public boolean isExpired(String token) {
		return Jwts
			.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getExpiration()
			.before(new Date());
	}

	public String getCategory(String token) {
		return Jwts
			.parser()
			.verifyWith(secretKey)
			.build().parseSignedClaims(token)
			.getPayload()
			.get("category", String.class);
	}

	public String createJwt(String category, String username, String role, Long expiredMs) {
		return Jwts.builder()
			.claim("category", category)
			.claim("username", username)
			.claim("role", role)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiredMs))
			.signWith(secretKey)
			.compact();
	}

	public void validateToken(String token) throws ExpiredJwtException, SignatureException {
		// 토큰 파싱 및 유효성 검사
		Jwts.parser()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token);
	}
}