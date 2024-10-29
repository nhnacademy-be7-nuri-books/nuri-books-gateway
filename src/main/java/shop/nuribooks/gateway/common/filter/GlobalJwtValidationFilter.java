package shop.nuribooks.gateway.common.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.jsonwebtoken.ExpiredJwtException;
import reactor.core.publisher.Mono;
import shop.nuribooks.gateway.common.util.JwtUtils;

/**
 * 매 요청에서 jwt 토큰이 만료했는 지 확인
 *
 * <P>
 *     토큰이 만료되었다면 요청 그대로 처리하고,
 *     토큰이 만료되었다면 인증 서버로 재발급한 토큰을 본래 요청한 응답 헤더에 담아서 처리
 *
 *     현재는 BOOKS API는 모두 해당 필터를 통한다.
 * </P>
 * @author nuri
 */
@Component
public class GlobalJwtValidationFilter implements GatewayFilterFactory<GlobalJwtValidationFilter.Config> {

	private final WebClient webClient;
	private final JwtUtils jwtUtils;

	/**
	 * 생성자
	 *
	 * <p>
	 *     재갱신 시 포워드 할 정보를 포함함으로 url 은 게이트웨이 정보로 등록
	 * </p>
	 *
	 * @param webClientBuilder spring WebFlux 에서 비동기 HTTP 요청을 보내기 위해 사용되는 WebClient 의 빌더 클래스
	 * @param jwtUtils 토큰 검증 유틸리티
	 */
	public GlobalJwtValidationFilter(WebClient.Builder webClientBuilder, JwtUtils jwtUtils) {
		this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
		this.jwtUtils = jwtUtils;
	}

	/**
	 * 토큰 검증 필터
	 *
	 * @param config GlobalJwtValidationFilter.Config
	 * @return chain.filter
	 */
	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {
			// Prev 요청의 헤더에서 jwt 가져오기
			String accessToken = exchange.getRequest()
				.getHeaders()
				.getFirst("Authorization");
			String refreshToken = exchange.getRequest()
				.getHeaders()
				.getFirst("Refresh");

			if (accessToken == null && refreshToken == null) {
				return chain.filter(exchange);
			}

			// AcceptToken 이 만료되지 않았다면 prev 요청 그대로
			try {
				jwtUtils.validateToken(accessToken);
				return chain.filter(exchange);
				// AcceptToken 이 만료되었다면 재발행 요청
			} catch (ExpiredJwtException e) {
				// 재발행 요청
				return webClient.post()
					.uri("/api/auth/reissue")
					.headers(headers -> {
						// jwt 를 재발행 요청 헤더에 추가
						headers.add("Authorization", accessToken);
						headers.add("Refresh", refreshToken);
					})
					.retrieve()
					.toBodilessEntity() // 응답 본문을 무시하고 헤더만 가져옴
					.flatMap(responseEntity -> {

						// 재발행한 토큰
						String newAcceptToken = responseEntity.getHeaders().getFirst("Authorization");
						String newRefreshToken = responseEntity.getHeaders().getFirst("Set-Cookie");
						// perv 요청 헤더의 토큰을 재발핼 된 토큰으로 변경
						// 원래 요청하려면 prev 요청 생성
						ServerHttpRequest mutatedRequest = exchange.getRequest()
							.mutate()
							.header("Authorization", newAcceptToken != null ? newAcceptToken : "")
							.header("Set-Cookie", newRefreshToken != null ? newRefreshToken : "")
							.build();

						// prev 응답에 토큰 정보 담아 응답
						ServerHttpResponse response = exchange.getResponse();
						response.beforeCommit(() -> {
							response.getHeaders().add("Authorization", newAcceptToken != null ? newAcceptToken : "");
							response.getHeaders().add("Set-Cookie", newRefreshToken != null ? newRefreshToken : "");
							return Mono.empty();
						});

						// 생성한 요청으로 요청
						return chain.filter(exchange.mutate().request(mutatedRequest).build());
					});
			}
		};
	}

	public static class Config {
	}
}
