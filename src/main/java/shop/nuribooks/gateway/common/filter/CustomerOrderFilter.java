package shop.nuribooks.gateway.common.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;

import shop.nuribooks.gateway.common.filter.rewrite.CustomerOrderRegisterRewrite;

/*
 * 회원 가입 시 발생하는 필터
 *
 * @author nuri
 */
@Component
public class CustomerOrderFilter extends AbstractGatewayFilterFactory<CustomerOrderFilter.Config> {

	private final ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;
	private final CustomerOrderRegisterRewrite customerOrderRegisterRewrite;

	public CustomerOrderFilter(
		ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter,
		CustomerOrderRegisterRewrite customerOrderRegisterRewrite) {
		super(Config.class);
		this.modifyRequestBodyFilter = modifyRequestBodyFilter;
		this.customerOrderRegisterRewrite = customerOrderRegisterRewrite;
	}

	/**
	 * 비회원 주문 시 requestBody 중 비밀번호 변경하는 필터
	 *
	 * @param config 필터 설정에 필요한 구성 정보
	 * @return GatewayFilter 객체로, 다음 필터 체인으로 요청을 전달하는 역할을 수행
	 */
	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) ->
			modifyRequestBodyFilter
				.apply(
					new ModifyRequestBodyGatewayFilterFactory.Config()
						.setRewriteFunction(String.class, String.class, customerOrderRegisterRewrite))
				.filter(exchange, chain);
	}

	public static class Config {
	}
}