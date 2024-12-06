package shop.nuribooks.gateway.common.filter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SignupFilterTest {

	@Mock
	private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

	@Mock
	private ServerWebExchange exchange;

	@Mock
	private GatewayFilterChain chain;

	@InjectMocks
	private SignupFilter signupFilter;

	@Test
	void testApply() {
		// given
		GatewayFilter mockFilter = (exchange1, chain2) -> Mono.empty();

		when(modifyRequestBodyFilter.apply((ModifyRequestBodyGatewayFilterFactory.Config)any())).thenReturn(
			mockFilter);

		// when
		Mono<Void> result = signupFilter.apply(new SignupFilter.Config()).filter(exchange, chain);

		// then
		StepVerifier.create(result)
			.verifyComplete();
	}
}