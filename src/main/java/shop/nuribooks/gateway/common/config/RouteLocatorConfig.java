package shop.nuribooks.gateway.common.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import shop.nuribooks.gateway.common.filter.AdminValidationFilter;
import shop.nuribooks.gateway.common.filter.CustomerOrderFilter;
import shop.nuribooks.gateway.common.filter.LoginFilter;
import shop.nuribooks.gateway.common.filter.MemberModifyFilter;
import shop.nuribooks.gateway.common.filter.SignupFilter;

/**
 * Gateway router
 * @author : nuri
 */
@Configuration
public class RouteLocatorConfig {

	public static final String BOOK_ROUTER = "lb://books";

	// 로그인 필터
	private final LoginFilter loginFilter;

	// 로그아웃 필터
	private final SignupFilter signupFilter;

	// 관리자 검증 필터
	private final AdminValidationFilter adminValidationFilter;

	// 멤버 수정 필터
	private final MemberModifyFilter memberModifyFilter;

	// 주문 필터
	private final CustomerOrderFilter customerOrderFilter;

	public RouteLocatorConfig(
		LoginFilter loginFilter,
		SignupFilter signupFilter,
		AdminValidationFilter adminValidationFilter,
		MemberModifyFilter memberModifyFilter,
		CustomerOrderFilter customerOrderFilter) {
		this.loginFilter = loginFilter;
		this.signupFilter = signupFilter;
		this.adminValidationFilter = adminValidationFilter;
		this.memberModifyFilter = memberModifyFilter;
		this.customerOrderFilter = customerOrderFilter;
	}

	/**
	 * 라우트 설정
	 * @param builder RouteLocatorBuilder 객체
	 * @return 설정된 RouteLocator 객체
	 */
	@Bean
	public RouteLocator myRoute(RouteLocatorBuilder builder) {

		return builder.routes()
			// ADMIN
			.route("admin_route", p -> p.path("/admin/**")
				.filters(f -> f.stripPrefix(1)
					.filter(adminValidationFilter.apply(new AdminValidationFilter.Config())))
				.uri(BOOK_ROUTER)
			)
			// BOOK
			.route("books_route",
				p -> p.path("/api/books/**", "/api/categories/**", "/api/contributors/**", "/api/reviews/**",
						"/api/publishers/**", "/api/book-tags/**", "/api/cart/**", "/api/coupon-policies/**",
						"/api/coupon-templates/**", "/api/member-coupons/**", "/api/image/**")
					.uri(BOOK_ROUTER)
			)
			// CUSTOMER ORDER REGISTER
			.route("order_register_route",
				p -> p.path("/api/orders")
					.and().method("POST")
					.filters(f -> f.filter(customerOrderFilter.apply(new CustomerOrderFilter.Config())))
					.uri(BOOK_ROUTER)
			)
			// ORDER
			.route("orders_route",
				p -> p.path("/api/orders/**", "/api/payments/**", "/api/shippings/**", "/api/wrapping/**")
					.uri(BOOK_ROUTER)
			)
			// MEMBER REGISTER
			.route("member_register_route",
				p -> p.path("/api/members")
					.and().method("POST")
					.filters(f -> f.filter(signupFilter.apply(new SignupFilter.Config())))
					.uri(BOOK_ROUTER)
			)
			// MEMBER MODIFY
			.route("member_modify",
				p -> p.path("/api/members/me")
					.and().method("PUT")
					.filters(f -> f.filter(memberModifyFilter.apply(new MemberModifyFilter.Config())))
					.uri(BOOK_ROUTER)
			)
			// MEMBER
			.route("member_route",
				p -> p.path("/api/members/**", "/api/point-policies/**",
						"/api/point-history/**")
					.uri(BOOK_ROUTER)
			)
			.route("auth_login",
				p -> p.path("/api/auth/login")
					.filters(f -> f.filter(loginFilter.apply(new LoginFilter.Config())))
					.uri("lb://auth")
			)
			.route("auth_route",
				p -> p.path("/api/auth/**")
					.uri("lb://auth")
			)
			.build();
	}
}
