package shop.nuribooks.gateway.common.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * gateway 로 요청/응답온 메서드를 표시해주는 로그 필터
 * @author : nuri
 */
@Component
@Slf4j
public class LogFilter implements GlobalFilter {

	/**
	 * 게이트웨이를 통한 요청이 올 시 해당 요청과 관련된 로그 조회
	 *
	 * @param exchange 요청 및 응답 정보
	 * @param chain 다음 필터 체인
	 * @return 필터 적용 결과
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String method = exchange.getRequest().getMethod().name();
		String requestPath = exchange.getRequest().getURI().toString();

		log.info("request 정보: Method - {}, Path - {}", method, requestPath);

		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			int statusCode = exchange.getResponse().getStatusCode() != null
				? exchange.getResponse().getStatusCode().value() : 0;
			log.info("Outgoing response: Status - {}", statusCode);
		}));
	}
}
