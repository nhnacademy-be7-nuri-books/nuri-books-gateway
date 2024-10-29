package shop.nuribooks.gateway.common.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import shop.nuribooks.gateway.common.filter.GlobalJwtValidationFilter;
import shop.nuribooks.gateway.common.filter.LoginFilter;
import shop.nuribooks.gateway.common.filter.SignupFilter;

/**
 * Gateway router
 * @author : nuri
 */
@Configuration
public class RouteLocatorConfig {

	private final GlobalJwtValidationFilter globalJwtValidationFilter;
	private final LoginFilter loginFilter;
	private final SignupFilter signupFilter;

	public RouteLocatorConfig(GlobalJwtValidationFilter globalJwtValidationFilter, LoginFilter loginFilter,
		SignupFilter signupFilter) {
		this.globalJwtValidationFilter = globalJwtValidationFilter;
		this.loginFilter = loginFilter;
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
					.filters(f -> f.filter(globalJwtValidationFilter.apply(new GlobalJwtValidationFilter.Config())))
					.uri("lb://books")
			)
			.route("categories_route",
				p -> p.path("/api/categories/**")
					.filters(f -> f.filter(globalJwtValidationFilter.apply(new GlobalJwtValidationFilter.Config())))
					.uri("lb://books")
			)
			.route("authors_route",
				p -> p.path("/api/authors/**")
					.filters(f -> f.filter(globalJwtValidationFilter.apply(new GlobalJwtValidationFilter.Config())))
					.uri("lb://books")
			)
			.route("publishers_route",
				p -> p.path("/api/publishers/**")
					.filters(f -> f.filter(globalJwtValidationFilter.apply(new GlobalJwtValidationFilter.Config())))
					.uri("lb://books")
			)
			.route("member_route",
				p -> p.path("/api/member")
					.filters(f -> f.filter(signupFilter.apply(new SignupFilter.Config())))
					.uri("lb://books")
			)
			.route("member_route",
				p -> p.path("/api/member/**")
					.filters(f -> f.filter(globalJwtValidationFilter.apply(new GlobalJwtValidationFilter.Config())))
					.uri("lb://books")
			)
			.route("auth",
				p -> p.path("/api/auth/login")
					.filters(f -> f.filter(loginFilter.apply(new LoginFilter.Config())))
					.uri("lb://auth")
			)
			.route("auth",
				p -> p.path("/api/auth/**")
					.uri("lb://auth")
			)
			.build();
	}
}
