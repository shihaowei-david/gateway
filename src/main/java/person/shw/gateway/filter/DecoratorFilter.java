package person.shw.gateway.filter;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import person.shw.gateway.decorator.GatewayWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author shihaowei
 * @date 2020/7/9 2:52 下午
 */
public class DecoratorFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(new GatewayWebExchange(exchange));
    }
}
