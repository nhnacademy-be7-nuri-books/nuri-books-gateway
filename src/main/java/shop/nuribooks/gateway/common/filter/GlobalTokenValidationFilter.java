package shop.nuribooks.gateway.common.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import shop.nuribooks.gateway.common.util.JwtUtils;

/**
 * 공동 토큰 검증 필터
 *
 * @author nuri
 */
@Component
@Slf4j
public class GlobalTokenValidationFilter implements GlobalFilter {

	private final JwtUtils jwtUtils;

	public GlobalTokenValidationFilter(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		// access token 얻어오기
		String accessToken = exchange.getRequest()
			.getHeaders()
			.getFirst(HttpHeaders.AUTHORIZATION);

		// 토큰 없다면 다음 필터로
		if (accessToken == null) {
			return chain.filter(exchange);
		}

		// 재발행의 경우 필터 통과
		String path = exchange.getRequest().getURI().getPath();

		// 특정 라우터에서 필터를 제외
		if (path.equals("/api/auth/reissue")) {
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

			log.error("X-USER-ID, X-USER-ROLE 전달");
			return chain.filter(mutatedExchange);

		} catch (ExpiredJwtException r) {
			log.error("토큰 검증 에러 : 토큰이 만료되었습니다.");
			return unauthorizedResponse(exchange);
		}

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
