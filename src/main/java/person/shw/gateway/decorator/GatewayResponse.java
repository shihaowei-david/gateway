package person.shw.gateway.decorator;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.FastByteArrayOutputStream;
import person.shw.gateway.constant.LogKeyConst;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * @author shihaowei
 * @date 2020/7/9 10:26 上午
 */
public class GatewayResponse extends ServerHttpResponseDecorator {


    private static final Logger LOG = LoggerFactory.getLogger("accessLog");

    private static final int LOG_MAX_REQUEST_CONTENT_SIZE = 1024 * 10;

    private static final int LOG_MAX_RESPONSE_CONTENT_SIZE = 1024 * 10;

    private final FastByteArrayOutputStream fastByteArrayOutputStream;

    public GatewayResponse(GatewayWebExchange exchange, ServerHttpResponse delegate) {
        super(delegate);
        fastByteArrayOutputStream = new FastByteArrayOutputStream();
        delegate.beforeCommit(() -> {
            long responseTime = System.currentTimeMillis() - (long) exchange.getAttribute(LogKeyConst.STOP_WATCH);

            GatewayRequest request = exchange.getGatewayRequest();
            GatewayResponse response = exchange.getGatewayResponse();

            URI uri = request.getURI();
            String requestPath = uri.getPath();
            if (StringUtils.startsWithIgnoreCase(requestPath, "/internalHeartBeat/_check")) {
                return Mono.empty();
            }

            /*LogMsg logMsg = LogMsg.build("GwAPIAccess")
                    .add("x_traceId", request.getTraceId())
                    .add("x_userId", request.getUserId())
                    .add("token", request.getToken())
                    .add("remoteIP", IpUtils.getIP(request))
                    //.add("statusCode",he.getStatusInt())
                    .add("requestRunningTimeMS", responseTime)
                    //.add("requestProtocol",)
                    .add("requestHost", uri.getHost())
                    .add("requestPort", uri.getPort())
                    .add("requestUri", requestPath)
                    .add("requestQueryString", uri.getQuery())
                    .add("statusCode", response.getStatusCode());

            byte[] requestBodyBytes = request.getBodyBytes();
            if (requestBodyBytes != null && requestBodyBytes.length <= LOG_MAX_REQUEST_CONTENT_SIZE) {
                logMsg.add("requestContent", new String(requestBodyBytes, StandardCharsets.UTF_8));
            } else {
                logMsg.add("requestContent", StringUtils.EMPTY);
            }

            byte[] responseBodyBytes = fastByteArrayOutputStream.toByteArray();
            if (responseBodyBytes.length <= LOG_MAX_RESPONSE_CONTENT_SIZE) {
                logMsg.add("responseContent", new String(responseBodyBytes, StandardCharsets.UTF_8));
            } else {
                logMsg.add("responseContent", StringUtils.EMPTY);
            }

            LOG.info("{}", logMsg);*/
            return Mono.empty();
        });
    }

    @Override
    public void beforeCommit(Supplier<? extends Mono<Void>> action) {
        super.beforeCommit(action);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(Flux.from(body).map(buffer ->
        {
            readBuffer(fastByteArrayOutputStream, buffer);
            return buffer;
        }));
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return super.writeAndFlushWith(Flux.from(body).map(publisher ->
                Flux.from(publisher).map(buffer ->
                {
                    readBuffer(fastByteArrayOutputStream, buffer);
                    return buffer;
                })));
    }

    private void readBuffer(FastByteArrayOutputStream fastByteArrayOutputStream, DataBuffer buffer) {
        try {
            Channels.newChannel(fastByteArrayOutputStream).write(buffer.asByteBuffer().asReadOnlyBuffer());
        } catch (IOException ignored) {

        }
    }


}
