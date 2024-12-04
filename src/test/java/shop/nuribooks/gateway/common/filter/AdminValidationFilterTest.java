package shop.nuribooks.gateway.common.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import shop.nuribooks.gateway.common.util.JwtUtils;

@ExtendWith(MockitoExtension.class)
class AdminValidationFilterTest {

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private ServerWebExchange exchange;

	@Mock
	private ServerHttpRequest request;

	@Mock
	private ServerHttpResponse response;

	@Mock
	private GatewayFilterChain chain;

	@InjectMocks
	private AdminValidationFilter adminValidationFilter;

	@Value("${header.refresh-key-name}")
	private String refreshHeaderName;

	@Test
	@DisplayName("ADMIN 권한 검증 필터 테스트 - 검증 성공")
	void adminRoleWithValidTokenTest() {
		// given
		String accessToken = "valid.jwt.token";

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, accessToken);

		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getRequest().getHeaders()).thenReturn(headers);

		doNothing().when(jwtUtils).validateToken(accessToken);
		when(jwtUtils.getRole(accessToken)).thenReturn(AdminValidationFilter.ROLE_ADMIN);
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		AdminValidationFilter.Config config = new AdminValidationFilter.Config();

		// when
		Mono<Void> result = adminValidationFilter.apply(config).filter(exchange, chain);

		// then
		assertNotNull(result, "Expected Mono<Void> to be non-null");

		StepVerifier.create(result)
			.verifyComplete();
	}

	@Test
	@DisplayName("ADMIN 권한 검증 필터 테스트 - accessToken Null")
	void adminRoleWithInValidTokenTest() {
		// given
		String accessToken = null;
		HttpHeaders headers = new HttpHeaders();
		DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		AdminValidationFilter.Config config = new AdminValidationFilter.Config();

		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getRequest().getHeaders()).thenReturn(headers);
		when(exchange.getResponse()).thenReturn(response);
		when(exchange.getResponse().getHeaders()).thenReturn(headers);
		when(response.bufferFactory()).thenReturn(bufferFactory);

		// when
		Mono<Void> result = adminValidationFilter.apply(config).filter(exchange, chain);

		// then
		verify(exchange, times(5)).getResponse();
	}
}