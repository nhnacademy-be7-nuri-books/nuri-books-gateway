package shop.nuribooks.gateway.common.filter;

import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import shop.nuribooks.gateway.common.util.JwtUtils;

/**
 * 로그인 적용 필터
 *
 * @author nuri
 */
@Component
public class LoginFilter extends AbstractGatewayFilterFactory<LoginFilter.Config> {

	private final JwtUtils jwtUtils;

	public LoginFilter(JwtUtils jwtUtils) {
		super(Config.class);
		this.jwtUtils = jwtUtils;
	}

	/**
	 * 로그인 성공 시 사용자 이름 헤더 반환
	 *
	 * @param config 필터 설정에 필요한 구성 정보
	 * @return GatewayFilter 객체로, 다음 필터 체인으로 요청을 전달하는 역할을 수행
	 */
	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpResponse response = exchange.getResponse();
			response.beforeCommit(() -> {
				String jwtToken = response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
				if (jwtToken != null) {
					String token = jwtToken.substring(7);
					String userId = jwtUtils.getUserId(token);
					response.getHeaders().add("X-USER-ID", userId);
				}
				return Mono.empty();
			});

			return chain.filter(exchange);
		};
	}

	public static class Config {
	}
}
