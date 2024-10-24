package shop.nuribooks.gateway.common.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway router
 * @author : nuri
 */
@Configuration
public class RouteLocatorConfig {

	/**
	 * 라우트 설정
	 * @param builder RouteLocatorBuilder 객체
	 * @return 설정된 RouteLocator 객체
	 */
	@Bean
	public RouteLocator myRoute(RouteLocatorBuilder builder) {

		return builder.routes()
			.route("books",
				p -> p.path("/api/books/**")
					.or()
					.path("/api/categories/**")
					.or()
					.path("/api/authors/**")
					.or()
					.path("/api/publishers/**")
					.or()
					.path("/api/member/**")
					.uri("lb://books")
			)
			.route("view",
				p -> p.path("/api/view/**").uri("lb://view")
			)
			.route("auth",
				p -> p.path("/api/auth/**")
					.uri("lb://auth")
			)
			.build();
	}
}
