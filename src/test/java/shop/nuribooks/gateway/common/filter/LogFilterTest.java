package shop.nuribooks.gateway.common.filter;

import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LogFilterTest {
	@Mock
	private ServerWebExchange exchange;

	@Mock
	private GatewayFilterChain chain;

	@Mock
	private ServerHttpRequest request;

	@Mock
	private ServerHttpResponse response;

	@InjectMocks
	private LogFilter logFilter;

	@Test
	@DisplayName("로그 필터 테스트")
	void logFilterTest() {

		//given
		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getRequest().getMethod()).thenReturn(HttpMethod.GET);
		when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/book"));

		when(exchange.getResponse()).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.OK);

		when(chain.filter(any())).thenReturn(Mono.empty());

		// when
		Mono<Void> result = logFilter.filter(exchange, chain);

		// Then
		StepVerifier.create(result)
			.verifyComplete();
	}

	@Test
	@DisplayName("로그 필터 테스트 - status Null")
	void logFilterNullStatueTest() {

		//given
		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getRequest().getMethod()).thenReturn(HttpMethod.GET);
		when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/book"));

		when(exchange.getResponse()).thenReturn(response);
		when(response.getStatusCode()).thenReturn(null);

		when(chain.filter(any())).thenReturn(Mono.empty());

		// when
		Mono<Void> result = logFilter.filter(exchange, chain);

		// Then
		StepVerifier.create(result)
			.verifyComplete();
	}
}