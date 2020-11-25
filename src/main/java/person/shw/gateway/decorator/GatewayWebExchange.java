package person.shw.gateway.decorator;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

/**
 * @author shihaowei
 * @date 2020/7/9 10:27 上午
 */
public class GatewayWebExchange extends ServerWebExchangeDecorator {

    private GatewayResponse gatewayResponse;
    private GatewayRequest gatewayRequest;

    public GatewayWebExchange(ServerWebExchange delegate) {
        super(delegate);
        gatewayRequest = new GatewayRequest(delegate.getRequest());
        gatewayResponse = new GatewayResponse(this, delegate.getResponse());
    }

    @Override
    public ServerHttpRequest getRequest() {
        return getGatewayRequest();
    }

    @Override
    public ServerHttpResponse getResponse() {
        return getGatewayResponse();
    }

    public GatewayRequest getGatewayRequest() {
        return gatewayRequest;
    }

    public void setGatewayRequest(GatewayRequest gatewayRequest) {
        this.gatewayRequest = gatewayRequest;
    }

    public GatewayResponse getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(GatewayResponse gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }
}
