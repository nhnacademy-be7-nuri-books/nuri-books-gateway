package shop.nuribooks.gateway.common.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import shop.nuribooks.gateway.common.filter.MemberJwtFilter;
import shop.nuribooks.gateway.common.filter.SignupFilter;

/**
 * Gateway router
 * @author : nuri
 */
@Configuration
public class RouteLocatorConfig {

	private final MemberJwtFilter memberJwtFilter;
	private final SignupFilter signupFilter;

	public RouteLocatorConfig(MemberJwtFilter memberJwtFilter, SignupFilter signupFilter) {
		this.memberJwtFilter = memberJwtFilter;
		this.signupFilter = signupFilter;
	}

	/**
	 * 라우트 설정
	 * @param builder RouteLocatorBuilder 객체
	 * @return 설정된 RouteLocator 객체
	 */
	@Bean
	public RouteLocator myRoute(RouteLocatorBuilder builder) {

		return builder.routes()
			.route("books_route",
				p -> p.path("/api/books/**")
					.uri("lb://books")
			)
			.route("categories_route",
				p -> p.path("/api/categories/**")
					.uri("lb://books")
			)
			.route("authors_route",
				p -> p.path("/api/authors/**")
					.uri("lb://books")
			)
			.route("publishers_route",
				p -> p.path("/api/publishers/**")
					.uri("lb://books")
			)
			.route("member_route",
				p -> p.path("/api/member")
					.filters(f -> f.filter(signupFilter.apply(new SignupFilter.Config())))
					.uri("lb://books")
			)
			.route("member_route",
				p -> p.path("/api/member/**")
					.uri("lb://books")
			)
			.route("auth",
				p -> p.path("/api/auth/login")
					.uri("lb://auth")
			)
			.route("auth",
				p -> p.path("/api/auth/**")
					.uri("lb://auth")
			)
			.build();
	}
}
