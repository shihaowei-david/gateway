package person.shw.gateway.filter.support;

import org.springframework.web.server.WebFilterChain;
import person.shw.gateway.decorator.GatewayWebExchange;
import person.shw.gateway.filter.AbstractGatewayFilter;
import reactor.core.publisher.Mono;

/**
 * @author shihaowei
 * @date 2020/7/9 4:36 下午
 */
public class ApiSignFilter extends AbstractGatewayFilter {
    @Override
    protected Mono<Boolean> doFilter(GatewayWebExchange exchange, WebFilterChain chain) {
        return Mono.just(true);
    }

    @Override
    protected Mono<Void> doIfDeny(GatewayWebExchange exchange) {
        return null;
    }

    @Override
    protected boolean skip(GatewayWebExchange exchange) {
        return true;
    }
}
