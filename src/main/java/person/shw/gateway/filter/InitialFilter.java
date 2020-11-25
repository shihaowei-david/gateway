package person.shw.gateway.filter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import person.shw.gateway.constant.LogKeyConst;
import person.shw.gateway.decorator.GatewayRequest;
import person.shw.gateway.decorator.GatewayWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author shihaowei
 * @date 2020/7/9 2:55 下午
 */
public class InitialFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getAttributes().put(LogKeyConst.STOP_WATCH,System.currentTimeMillis());

        ServerHttpRequest request = exchange.getRequest();
        MediaType contentType = request.getHeaders().getContentType();
        long contentLength = request.getHeaders().getContentLength();
        HttpMethod method = request.getMethod();

        if (contentLength > 0 && Objects.equals(method, HttpMethod.POST)) {
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)){
                return handleJsonBody((GatewayWebExchange)exchange,chain);
            }
        }
        return null;
    }

    private Mono<Void> handleJsonBody(GatewayWebExchange exchange, WebFilterChain chain) {
        GatewayRequest request = exchange.getGatewayRequest();
        return DataBufferUtils.join(request.getBody()).flatMap(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);

            GatewayRequest gatewayRequest = new GatewayRequest(request){
                @Override
                public Flux<DataBuffer> getBody() {
                    return Flux.defer(() -> {
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                        DataBufferUtils.retain(buffer);
                        return Mono.just(buffer);
                    });
                }
            }.setBodyBytes(bytes);

            exchange.setGatewayRequest(gatewayRequest);
            return chain.filter(exchange);
        });

    }
}
