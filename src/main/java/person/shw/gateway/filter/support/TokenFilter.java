package person.shw.gateway.filter.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilterChain;
import person.shw.gateway.config.AuthConfig;
import person.shw.gateway.constant.ResultCode;
import person.shw.gateway.decorator.GatewayRequest;
import person.shw.gateway.decorator.GatewayWebExchange;
import person.shw.gateway.dto.ResultDTO;
import person.shw.gateway.exception.GatewayServiceException;
import person.shw.gateway.filter.AbstractGatewayFilter;
import person.shw.gateway.util.MonoUtils;
import reactor.core.publisher.Mono;

/**
 * @author shihaowei
 * @date 2020/7/9 4:11 下午
 */
public class TokenFilter extends AbstractGatewayFilter {


    private static final Logger LOG = LoggerFactory.getLogger(TokenFilter.class);

    private static final String KEY_RTN = "rtn";
    private static final String KEY_DATA = "data";
    private static final String KEY_ANONYMOUS_ID = "anonymousId";
    private static final String KEY_USER_ID = "userId";
    private static final String ASTERISK = "*";

    @Override
    protected Mono<Boolean> doFilter(GatewayWebExchange exchange, WebFilterChain chain) {
        GatewayRequest request = (GatewayRequest) exchange.getRequest();
        String token = request.getToken();
        return WebClient.create(AuthConfig.AUTH_SERVER + "/token/verifyForGateway?v=v2&token=" + token)
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(c -> {
                    //TODO wuweiqiang 考虑直接接入Redis校验
                    JSONObject jo = JSON.parseObject(c);
                    if (jo == null || jo.isEmpty()) {
                        return Mono.just(false);
                    }
                    Integer rtn = jo.getInteger(KEY_RTN);
                    if (rtn == null || rtn != 0) {
                        return Mono.just(false);
                    }
                    JSONObject data = jo.getJSONObject(KEY_DATA);
                    if (data == null || data.isEmpty()) {
                        return Mono.just(false);
                    }
                    String userId = data.getString(KEY_USER_ID);
                    if (StringUtils.isNotBlank(userId)) {
                        request.setUserId(Long.parseLong(userId));
                        return Mono.just(true);
                    }
                    return Mono.just(false);
                }).doOnError(e -> {
                    LOG.error("[TokenFilter] verify token error", e);
                    throw new GatewayServiceException(ResultCode.ERROR, "身份认证异常");
                });
    }

    @Override
    protected Mono<Void> doIfDeny(GatewayWebExchange exchange) {
        return MonoUtils.responseJson(exchange, ResultDTO.unauthorized());
    }

    @Override
    protected boolean skip(GatewayWebExchange exchange) {
        GatewayRequest request = exchange.getGatewayRequest();
        String path = request.getPath().pathWithinApplication().value();
        return true;
        //return AuthConfig.WHITE_LIST.contains(path) || AuthConfig.WHITE_LIST.contains(ASTERISK);
    }
}
