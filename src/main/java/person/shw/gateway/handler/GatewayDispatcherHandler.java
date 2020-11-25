package person.shw.gateway.handler;

import org.apache.dubbo.common.extension.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import person.shw.gateway.bean.RouteMethod;
import person.shw.gateway.cache.DubboCache;
import person.shw.gateway.cache.RouteCache;
import person.shw.gateway.dubbo.DubboInvoker;
import reactor.core.publisher.Mono;

/**
 * @author shihaowei
 * @date 2020/7/8 11:38 上午
 */

public class GatewayDispatcherHandler extends DispatcherHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayDispatcherHandler.class);

    @Autowired
    private DubboInvoker dubboInvoker;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().pathWithinApplication().value();
        String routeKey = path + ":" + request.getMethod().name();
        RouteMethod routeMethod = RouteCache.ROUTE_MAP.get(routeKey);
        if (routeMethod != null) {
            return dubboInvoker.handleDubboRpc(exchange,routeMethod);
        }
        // 不是rpc调用就走默认的spring mvc的调用
        return super.handle(exchange);
    }
}
