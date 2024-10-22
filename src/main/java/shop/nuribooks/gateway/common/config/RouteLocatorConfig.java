package shop.nuribooks.gateway.common.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfig {

	@Bean
	public RouteLocator myRoute(RouteLocatorBuilder builder) {

		return builder.routes()
			.route("auth",
				p -> p.path("/api/auth/**").uri("lb://auth")
			)
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
			.build();
	}
}
