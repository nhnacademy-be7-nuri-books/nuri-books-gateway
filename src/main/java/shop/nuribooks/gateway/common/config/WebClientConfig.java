package shop.nuribooks.gateway.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@link WebClientConfig}는 Spring WebClient 를 구성하는 설정 클래스
 *
 */
@Configuration
public class WebClientConfig {

	/**
	 * WebClient.Builder 빈을 생성합니다.
	 *
	 * @return WebClient.Builder 의 인스턴스
	 */
	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}
}
