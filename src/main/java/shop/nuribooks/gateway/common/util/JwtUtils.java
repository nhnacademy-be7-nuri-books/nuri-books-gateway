package shop.nuribooks.gateway.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

/**
 *  jwt Utils
 *  @author : yongtaek
 */
@Component
public class JwtUtils {
	private final SecretKey secretKey;

	public JwtUtils(@Value("${spring.jwt.secret}") String secret) {
		this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	public String getUserId(String token) {
		return Jwts
			.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("userId", String.class);
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

	public void validateToken(String token) throws ExpiredJwtException {
		// 토큰 파싱 및 유효성 검사
		Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token);
	}
}
