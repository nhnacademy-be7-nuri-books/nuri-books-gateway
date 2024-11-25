package shop.nuribooks.gateway.common.filter.rewrite;

import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 비회원 주문의 본문을 재작성하는 필터
 *
 * <p>
 * BCryptPasswordEncoder를 사용하여 비밀번호를 해시화
 * </p>
 *
 * @author : nuri
 */
@Slf4j
@Service
public class CustomerOrderRegisterRewrite implements RewriteFunction<String, String> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 주어진 요청 본문에서 비밀번호를 해시화
	 *
	 * @param serverWebExchange 요청에 대한 정보
	 * @param body 원본 요청 본문
	 * @return 해시화된 비밀번호를 포함한 새로운 요청 본문
	 */
	@Override
	public Publisher<String> apply(ServerWebExchange serverWebExchange, String body) {

		try {
			Map<String, Object> map = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
			});

			processPassword(map);

			return Mono.just(objectMapper.writeValueAsString(map));

		} catch (JsonProcessingException e) {

			log.error("RequestBodyRewrite 의 request body 를 json 으로 변환하는 중 예외가 발생했습니다.");

			throw new RuntimeException("RequestBodyRewrite 의 request body 를 json 으로 변환하는 중 예외가 발생했습니다.");
		}
	}

	private void processPassword(Map<String, Object> map) {
		BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getKey().equals("customerRegister")) {
				Map<String, Object> subMap = (Map<String, Object>)entry.getValue();
				if (subMap != null && subMap.containsKey("password")) {
					String prevPassword = (String)subMap.get("password");
					String changedPassword = bcryptPasswordEncoder.encode(prevPassword);
					subMap.put("password", changedPassword);
				}
			}
		}
	}
}
