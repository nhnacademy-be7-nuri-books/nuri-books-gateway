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

class CustomerOrderRegisterRewriteTest {

	@Mock
	private ServerWebExchange exchange;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final CustomerOrderRegisterRewrite customerOrderRegisterRewrite = new CustomerOrderRegisterRewrite();

	@Test
	@DisplayName("비회원 주문 등록 비밀번호 암호화 필터 테스트 - 정상처리")
	void processPasswordEncryptSuccessTest() {
		// Given
		String originalRequestBody = "";

		Map<String, Object> customerRegister = new HashMap<>();
		customerRegister.put("password", "plainPassword123");

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("customerRegister", customerRegister);

		try {
			originalRequestBody = objectMapper.writeValueAsString(requestBody);
		} catch (JsonProcessingException e) {
			fail("Failed to create test JSON.");
		}

		// When
		Mono<String> resultMono = (Mono<String>)customerOrderRegisterRewrite.apply(exchange, originalRequestBody);

		// Then
		resultMono.publishOn(Schedulers.boundedElastic()).doOnTerminate(() -> {
			try {
				String result = resultMono.block();
				Map<String, Object> resultMap = objectMapper.readValue(result,
					new TypeReference<Map<String, Object>>() {
					});

				@SuppressWarnings("unchecked")
				Map<String, Object> resultCustomerRegister = (Map<String, Object>)resultMap.get("customerRegister");

				String hashedPassword = (String)resultCustomerRegister.get("password");
				assertNotNull(hashedPassword);
				assertTrue(hashedPassword.startsWith("$2a$"));
				assertNotEquals("plainPassword123", hashedPassword);

			} catch (JsonProcessingException e) {
				fail("Failed to parse result body.");
			}
		}).block();
	}

	@Test
	@DisplayName("비회원 주문 등록 비밀번호 암호화 필터 테스트 - customerRegister 없음")
	void processPasswordEncryptCustomerRegisterNotExistsTest() {
		// Given
		String originalRequestBody = "";

		Map<String, Object> customerRegister = new HashMap<>();
		customerRegister.put("password", "plainPassword123");

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("customer", customerRegister);

		try {
			originalRequestBody = objectMapper.writeValueAsString(requestBody);
		} catch (JsonProcessingException e) {
			fail("Failed to create test JSON.");
		}

		// When
		Mono<String> resultMono = (Mono<String>)customerOrderRegisterRewrite.apply(exchange, originalRequestBody);

		// Then
		resultMono.publishOn(Schedulers.boundedElastic()).doOnTerminate(() -> {
			try {
				String result = resultMono.block();
				Map<String, Object> resultMap = objectMapper.readValue(result,
					new TypeReference<Map<String, Object>>() {
					});

				@SuppressWarnings("unchecked")
				Map<String, Object> resultCustomerRegister = (Map<String, Object>)resultMap.get("customer");

				String hashedPassword = (String)resultCustomerRegister.get("password");
				assertEquals("plainPassword123", hashedPassword);

			} catch (JsonProcessingException e) {
				fail("Failed to parse result body.");
			}
		}).block();
	}

	@Test
	@DisplayName("비회원 주문 등록 비밀번호 암호화 필터 테스트 - subMap 없음")
	void processPasswordEncryptSubMapNotExistsTest() {
		// Given
		String originalRequestBody = "";

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("customerRegister", null);

		try {
			originalRequestBody = objectMapper.writeValueAsString(requestBody);
		} catch (JsonProcessingException e) {
			fail("Failed to create test JSON.");
		}

		// When
		Mono<String> resultMono = (Mono<String>)customerOrderRegisterRewrite.apply(exchange, originalRequestBody);

		// Then
		resultMono.publishOn(Schedulers.boundedElastic()).doOnTerminate(() -> {
			try {
				String result = resultMono.block();
				Map<String, Object> resultMap = objectMapper.readValue(result,
					new TypeReference<Map<String, Object>>() {
					});

				@SuppressWarnings("unchecked")
				Map<String, Object> resultCustomerRegister = (Map<String, Object>)resultMap.get("customer");

				assertNull(resultCustomerRegister);

			} catch (JsonProcessingException e) {
				fail("Failed to parse result body.");
			}
		}).block();
	}

	@Test
	@DisplayName("비회원 주문 등록 비밀번호 암호화 필터 테스트 - 비밀번호 없음")
	void processPasswordEncryptPasswordNotExistsTest() {
		// Given
		String originalRequestBody = "";

		Map<String, Object> customerRegister = new HashMap<>();
		customerRegister.put("name", "plainPassword123");

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("customerRegister", customerRegister);

		try {
			originalRequestBody = objectMapper.writeValueAsString(requestBody);
		} catch (JsonProcessingException e) {
			fail("Failed to create test JSON.");
		}

		// When
		Mono<String> resultMono = (Mono<String>)customerOrderRegisterRewrite.apply(exchange, originalRequestBody);

		// Then
		resultMono.publishOn(Schedulers.boundedElastic()).doOnTerminate(() -> {
			try {
				String result = resultMono.block();
				Map<String, Object> resultMap = objectMapper.readValue(result,
					new TypeReference<Map<String, Object>>() {
					});

				@SuppressWarnings("unchecked")
				Map<String, Object> resultCustomerRegister = (Map<String, Object>)resultMap.get("customerRegister");

				String hashedPassword = (String)resultCustomerRegister.get("name");
				assertEquals("plainPassword123", hashedPassword);

			} catch (JsonProcessingException e) {
				fail("Failed to parse result body.");
			}
		}).block();
	}

	@Test
	@DisplayName("비회원 주문 등록 비밀번호 암호화 필터 테스트 - json 예외")
	void processPasswordEncryptExceptionTest() {
		// given
		String originalRequestBody = "";

		// when
		Mono<String> resultMono = (Mono<String>)customerOrderRegisterRewrite.apply(exchange, originalRequestBody);

		// then
		StepVerifier.create(resultMono)
			.expectError(RuntimeException.class)
			.verify();
	}
}