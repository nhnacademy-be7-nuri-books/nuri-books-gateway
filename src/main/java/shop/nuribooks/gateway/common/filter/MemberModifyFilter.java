package shop.nuribooks.gateway.common.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;

import shop.nuribooks.gateway.common.filter.rewrite.MemberPasswordRequestBodyRewrite;

/*
 * 회원 수정 시 발생하는 필터
 *
 * @author nuri
 */
@Component
public class MemberModifyFilter extends AbstractGatewayFilterFactory<MemberModifyFilter.Config> {

	private final ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;
	private final MemberPasswordRequestBodyRewrite requestBodyRewrite;

	public MemberModifyFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter,
		MemberPasswordRequestBodyRewrite requestBodyRewrite) {
		super(Config.class);
		this.modifyRequestBodyFilter = modifyRequestBodyFilter;
		this.requestBodyRewrite = requestBodyRewrite;
	}

	/**
	 * 회원 수정 시 시 requestBody 중 비밀번호 변경하는 필터
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
						.setRewriteFunction(String.class, String.class, requestBodyRewrite))
				.filter(exchange, chain);
	}

	public static class Config {
	}
}