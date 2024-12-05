package shop.nuribooks.gateway.common.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.ExpiredJwtException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import shop.nuribooks.gateway.common.util.JwtUtils;

@ExtendWith(MockitoExtension.class)
class GlobalTokenValidationFilterTest {
	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private ServerWebExchange exchange;

	@Mock
	private GatewayFilterChain chain;

	@Mock
	private ServerHttpRequest request;

	@Mock
	private ServerHttpResponse response;

	@InjectMocks
	private GlobalTokenValidationFilter globalTokenValidationFilter;

	@Test
	@DisplayName("토큰 검증 성공 - 유효한 토큰")
	void validTokenTest() {
		// given
		String accessToken = "valid.jwt.token";

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, accessToken);

		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getRequest().getHeaders()).thenReturn(headers);
		when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/book"));

		doNothing().when(jwtUtils).validateToken(accessToken);
		when(jwtUtils.getUserId(accessToken)).thenReturn("user123");
		when(jwtUtils.getRole(accessToken)).thenReturn("ROLE_USER");

		ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
		when(exchange.mutate()).thenReturn(exchangeBuilder);
		when(exchangeBuilder.request(any(ServerHttpRequest.class))).thenReturn(exchangeBuilder);
		when(exchangeBuilder.build()).thenReturn(exchange);
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		// when
		Mono<Void> result = globalTokenValidationFilter.filter(exchange, chain);

		// then
		StepVerifier.create(result)
			.expectSubscription()
			.verifyComplete();

	}

	@Test
	@DisplayName("토큰이 없을 때 필터 통과 테스트")
	void tokenAbsentTest() {
		// given
		HttpHeaders headers = new HttpHeaders();
		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getRequest().getHeaders()).thenReturn(headers);
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		// when
		Mono<Void> result = globalTokenValidationFilter.filter(exchange, chain);

		// then
		assertNotNull(result, "Expected Mono<Void> to be non-null");

		StepVerifier.create(result)
			.verifyComplete();
	}

	@Test
	@DisplayName("재갱신 url 일 경우 필터 통과 테스트")
	void tokenReissueAbsentTest() {
		// given
		String accessToken = "valid.jwt.token";

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, accessToken);

		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getRequest().getHeaders()).thenReturn(headers);
		when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/auth/reissue"));
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		// when
		Mono<Void> result = globalTokenValidationFilter.filter(exchange, chain);

		// then
		assertNotNull(result, "Expected Mono<Void> to be non-null");

		StepVerifier.create(result)
			.verifyComplete();
	}

	@Test
	@DisplayName("토큰 만료 예외 처리 테스트 - ExpiredJwtException")
	void tokenExpiredTest() {
		// given
		String accessToken = "expired.jwt.token";

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, accessToken);

		DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getRequest().getHeaders()).thenReturn(headers);
		when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/book"));

		when(exchange.getResponse()).thenReturn(response);
		when(exchange.getResponse().getHeaders()).thenReturn(headers);
		when(response.bufferFactory()).thenReturn(bufferFactory);
		when(response.writeWith(any())).thenReturn(Mono.empty());

		when(jwtUtils.getRole(accessToken)).thenThrow(ExpiredJwtException.class);

		// when
		Mono<Void> result = globalTokenValidationFilter.filter(exchange, chain);

		// then
		assertNotNull(result, "Expected Mono<Void> to be non-null");

		StepVerifier.create(result)
			.verifyComplete();
	}
}