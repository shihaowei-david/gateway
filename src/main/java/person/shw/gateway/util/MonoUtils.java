package person.shw.gateway.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * @author shihaowei
 * @date 2020/7/9 4:15 下午
 */
public class MonoUtils {

    private static final SimplePropertyPreFilter JSON_CLASS_FILTER = new SimplePropertyPreFilter();

    static {
        JSON_CLASS_FILTER.getExcludes().add("class");
    }

    public static Mono<Void> responseJson(ServerWebExchange exchange, Object result) {
        if (result instanceof Map) {
            Map map = (Map) result;
            map.remove("code");
            map.remove("failure");
            map.remove("success");
        }

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(Objects.requireNonNull(JSON.toJSONBytes(result, JSON_CLASS_FILTER)))));
    }

    public static Mono<Void> responseString(ServerWebExchange exchange, HttpStatus status, String text) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.TEXT_HTML);
        response.setStatusCode(status);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(text.getBytes())));
    }

    public static Mono<Void> responseString(ServerWebExchange exchange, String text) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.TEXT_HTML);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(text.getBytes())));
    }
}
