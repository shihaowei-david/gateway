package person.shw.gateway.filter;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import person.shw.gateway.decorator.GatewayWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author shihaowei
 * @date 2020/7/9 4:01 下午
 */
public abstract class AbstractGatewayFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange decorator, WebFilterChain chain) {
        GatewayWebExchange exchange = null;

        if (decorator instanceof GatewayWebExchange) {
            exchange = (GatewayWebExchange) decorator;
        }else {
            ServerWebExchange delegate = ((ServerWebExchangeDecorator) decorator).getDelegate();
            if (decorator instanceof GatewayWebExchange) {
                exchange = (GatewayWebExchange) decorator;
            }
        }


        final GatewayWebExchange gatewayWebExchange = exchange;
        if (skip((gatewayWebExchange))){
            return chain.filter(gatewayWebExchange);
        }else {
            return doFilter(gatewayWebExchange, chain)
                    .switchIfEmpty(Mono.just(false))
                    .flatMap(r -> r ? chain.filter(gatewayWebExchange) : doIfDeny(gatewayWebExchange));
        }

    }


    protected abstract Mono<Boolean> doFilter(GatewayWebExchange exchange, WebFilterChain chain);

    protected abstract Mono<Void> doIfDeny(GatewayWebExchange exchange);

    protected abstract boolean skip(GatewayWebExchange exchange);
}
