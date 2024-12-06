package shop.nuribooks.gateway.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
class JwtUtilsTest {

	@Autowired
	private JwtUtils jwtUtils;

	private String validToken;
	private String expiredToken;

	private final String userId = "user123";
	private final String role = "admin";
	private final long year = 86400000L * 365;
	private SecretKey secretKey;

	@BeforeEach
	void setUp() {
		jwtUtils = new JwtUtils("aVeryStrongSecretKeyForJwtTesting123!");
		secretKey = jwtUtils.getSecretKey();

		validToken = createValidToken();
		expiredToken = createExpiredToken();
	}

	private String createValidToken() {
		return Jwts.builder()
			.claim("userId", userId)
			.claim("role", role)
			.expiration(Date.from(Instant.now().plusMillis(year)))
			.signWith(secretKey)
			.compact();
	}

	private String createExpiredToken() {
		return Jwts.builder()
			.claim("userId", userId)
			.claim("role", role)
			.expiration(Date.from(Instant.now().minusMillis(year)))
			.signWith(secretKey)
			.compact();
	}

	@Test
	void testGetUserId() {
		String result = jwtUtils.getUserId(validToken);
		assertEquals(userId, result);
	}

	@Test
	void testGetRole() {
		String result = jwtUtils.getRole(validToken);
		assertEquals(role, result);
	}

	@Test
	void testIsExpired_whenExpiredToken() {
		assertThrows(ExpiredJwtException.class, () -> jwtUtils.validateToken(expiredToken));
	}

	@Test
	void testIsExpired_whenValidToken() {
		boolean result = jwtUtils.isExpired(validToken);
		assertFalse(result);
	}

	@Test
	void testValidateToken_whenValidToken() {
		assertDoesNotThrow(() -> jwtUtils.validateToken(validToken));
	}

	@Test
	void testValidateToken_whenExpiredToken() {
		assertThrows(ExpiredJwtException.class, () -> jwtUtils.validateToken(expiredToken));
	}
}