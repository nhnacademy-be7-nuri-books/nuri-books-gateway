package shop.nuribooks.gateway.common.filter.rewrite;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

class SignupRequestBodyRewriteTest {

	@Mock
	private ServerWebExchange exchange;

	private ObjectMapper objectMapper = new ObjectMapper();
	private SignupRequestBodyRewrite signupRequestBodyRewrite = new SignupRequestBodyRewrite();

	@Test
	@DisplayName("회원 가입 비밀번호 암호화 필터 테스트 - 정상 처리")
	void processPasswordEncryptSuccessTest() {
		// Given
		String originalRequestBody = "";

		Map<String, Object> customPasswordModify = new HashMap<>();
		customPasswordModify.put("name", "test");
		customPasswordModify.put("password", "plainPassword123");

		try {
			originalRequestBody = objectMapper.writeValueAsString(customPasswordModify);
		} catch (JsonProcessingException e) {
			fail("Failed to create test JSON.");
		}

		// When
		Mono<String> resultMono = (Mono<String>)signupRequestBodyRewrite.apply(exchange, originalRequestBody);

		// Then
		resultMono.publishOn(Schedulers.boundedElastic()).doOnTerminate(() -> {
			try {
				String result = resultMono.block();
				Map<String, Object> resultMap = objectMapper.readValue(result,
					new TypeReference<Map<String, Object>>() {
					});

				String hashedPassword = (String)resultMap.get("password");
				assertNotNull(hashedPassword);
				assertTrue(hashedPassword.startsWith("$2a$"));
				assertNotEquals("plainPassword123", hashedPassword);

			} catch (JsonProcessingException e) {
				fail("Failed to parse result body.");
			}
		}).block();

	}

	@Test
	@DisplayName("회원 가입 비밀번호 암호화 필터 테스트 - name이 없으면 비밀번호 변경하지 않음")
	void processPasswordEncryptNotExistsNameTest() {
		// Given
		String originalRequestBody = "";

		Map<String, Object> customPasswordModify = new HashMap<>();
		customPasswordModify.put("password", "plainPassword123");

		try {
			originalRequestBody = objectMapper.writeValueAsString(customPasswordModify);
		} catch (JsonProcessingException e) {
			fail("Failed to create test JSON.");
		}

		// When
		Mono<String> resultMono = (Mono<String>)signupRequestBodyRewrite.apply(exchange, originalRequestBody);

		// Then
		resultMono.publishOn(Schedulers.boundedElastic()).doOnTerminate(() -> {
			try {
				String result = resultMono.block();
				Map<String, Object> resultMap = objectMapper.readValue(result,
					new TypeReference<Map<String, Object>>() {
					});

				String hashedPassword = (String)resultMap.get("password");
				assertEquals("plainPassword123", hashedPassword);

			} catch (JsonProcessingException e) {
				fail("Failed to parse result body.");
			}
		}).block();

	}

	@Test
	@DisplayName("회원 가입 비밀번호 암호화 필터 테스트 - json 예외")
	void processPasswordEncryptExceptionTest() {
		// given
		String originalRequestBody = "";

		// when
		Mono<String> resultMono = (Mono<String>)signupRequestBodyRewrite.apply(exchange, originalRequestBody);

		// then
		StepVerifier.create(resultMono)
			.expectError(RuntimeException.class)
			.verify();
	}

}