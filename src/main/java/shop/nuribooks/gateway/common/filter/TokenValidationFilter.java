package shop.nuribooks.gateway.common.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.ExpiredJwtException;
import reactor.core.publisher.Mono;
import shop.nuribooks.gateway.common.util.JwtUtils;

/**
 * 공동 토큰 검증 필터
 *
 * @author nuri
 */
public class TokenValidationFilter extends AbstractGatewayFilterFactory<TokenValidationFilter.Config> {

	private final JwtUtils jwtUtils;

	public TokenValidationFilter(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	public static class Config {
	}

	/**
	 * 토큰이 있다면 헤더에 토큰 페이로드 정보를 일부 전달
	 *
	 * @param config GlobalTokenValidationFilter.config
	 * @return filter chain
	 */
	@Override
	public GatewayFilter apply(TokenValidationFilter.Config config) {
		return (exchange, chain) -> {

			// access token 얻어오기
			String accessToken = exchange.getRequest()
				.getHeaders()
				.getFirst(HttpHeaders.AUTHORIZATION);

			// 토큰 없다면 다음 필터로
			if (accessToken == null) {
				return chain.filter(exchange);
			}

			try {
				jwtUtils.validateToken(accessToken);

				String userId = jwtUtils.getUserId(accessToken);
				String role = jwtUtils.getRole(accessToken);

				HttpHeaders headers = new HttpHeaders();
				headers.putAll(exchange.getRequest().getHeaders());
				headers.add("X-USER-ID", userId);
				headers.add("X-USER-ROLE", role);

				// 새로운 ServerHttpRequestDecorator 를 생성하여 헤더를 교체
				ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
					@Override
					public HttpHeaders getHeaders() {
						return headers;
					}
				};

				// 변경된 요청으로 Exchange 생성
				ServerWebExchange mutatedExchange = exchange.mutate()
					.request(mutatedRequest)
					.build();

				return chain.filter(mutatedExchange);

			} catch (ExpiredJwtException r) {
				return unauthorizedResponse(exchange);
			}

		};
	}

	/**
	 * 401 UNAUTHORIZED 응답을 반환
	 *
	 * @param exchange 요청-응답 교환 객체
	 * @return 401 응답 반환
	 */
	private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		String errorResponseBody = "{\"status\": 401, \"message\": \"UNAUTHORIZED\", \"details\": \"UNAUTHORIZED\"}";

		return exchange.getResponse().writeWith(Mono.just(
			exchange.getResponse().bufferFactory().wrap(errorResponseBody.getBytes())
		));
	}
}
