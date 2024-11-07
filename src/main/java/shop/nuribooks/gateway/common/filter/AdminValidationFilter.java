package shop.nuribooks.gateway.common.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.ExpiredJwtException;
import reactor.core.publisher.Mono;
import shop.nuribooks.gateway.common.util.JwtUtils;

/**
 * Admin 관련 요청 jwt 토근 검증
 *
 * <p>
 * Admin 권한이 아니거나 토큰이 없다면 401 error
 * </p>
 * @author nuri
 */
@Component
public class AdminValidationFilter extends AbstractGatewayFilterFactory<AdminValidationFilter.Config> {

	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	private final JwtUtils jwtUtils;
	@Value("${header.refresh-key-name}")
	private String refreshHeaderName;

	/**
	 * 생성자
	 * @param jwtUtils jwt 처리 유틸리티
	 */
	public AdminValidationFilter(JwtUtils jwtUtils) {
		super(AdminValidationFilter.Config.class);
		this.jwtUtils = jwtUtils;
	}

	/**
	 * 관리자 인가 필터
	 *
	 * @param config AdminValidationFilter.Config
	 * @return chain.filter
	 */
	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			String accessToken = exchange.getRequest()
				.getHeaders()
				.getFirst(HttpHeaders.AUTHORIZATION);

			if (accessToken == null) {
				return unauthorizedResponse(exchange);
			}

			try {
				jwtUtils.validateToken(accessToken);

				String role = jwtUtils.getRole(accessToken);

				if (role.equals(ROLE_ADMIN)) {
					return chain.filter(exchange);
				}

				return unauthorizedResponse(exchange);

			} catch (ExpiredJwtException e) {
				return unauthorizedResponse(exchange);
			}

			// 다음 필터로 넘어가지 않고 필터 종료
			//return exchange.getResponse().setComplete();
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

	public static class Config {
	}
}
