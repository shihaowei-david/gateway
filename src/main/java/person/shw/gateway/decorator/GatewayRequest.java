package person.shw.gateway.decorator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import person.shw.gateway.constant.RequestKeyConst;
import person.shw.gateway.util.SnowFlakeIdGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shihaowei
 * @date 2020/7/9 10:25 上午
 */
public class GatewayRequest extends ServerHttpRequestDecorator {

    private static final String BEARER_PREFIX = "Bearer ";
    /** 请求跟踪Id */
    private long traceId;
    /** 请求Body */
    private byte[] bodyBytes;
    /** 请求Token对应的UserId */
    private Long userId;
    /** 请求Token */
    private final String token;
    /** Header的额外扩展信息 */
    private final Map<String, String> extMetadata;

    public GatewayRequest(ServerHttpRequest request) {
        super(request);

        this.traceId = SnowFlakeIdGenerator.nextSnowFlakeId();
        this.token = resolveToken(request);
        this.extMetadata = resolveExtMetadata(request);
    }

    public GatewayRequest(GatewayWebExchange exchange, GatewayRequest request) {
        super(request.getDelegate());

        this.traceId = request.getTraceId();
        this.bodyBytes = request.getBodyBytes();
        this.userId = request.getUserId();
        this.token = request.getToken();
        this.extMetadata = request.getExtMetadata();
    }

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public GatewayRequest setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
        return this;
    }

    /** 解析Token */
    private String resolveToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(RequestKeyConst.AUTHORIZATION);
        token = StringUtils.replaceIgnoreCase(token, BEARER_PREFIX, StringUtils.EMPTY);
        return token;
    }

    /** 解析ExtMetadata */
    private Map<String, String> resolveExtMetadata(ServerHttpRequest delegate) {
        List<String> list = delegate.getHeaders().get(RequestKeyConst.EXT_METADATA);
        if (list == null || list.size() == 0) {
            return null;
        }

        Map<String, String> extMetadata = new HashMap<>(32);
        for (String s : list) {
            if (StringUtils.isBlank(s)) {
                continue;
            }
            String[] items = StringUtils.split(s, ";");
            if (items == null || items.length == 0) {
                continue;
            }

            for (String i : items) {
                if (StringUtils.isBlank(i)) {
                    continue;
                }

                String[] kvs = StringUtils.split(i, "=");
                if (kvs == null || kvs.length == 0) {
                    extMetadata.put(i.trim(), null);
                } else {
                    extMetadata.put(kvs[0].trim(), kvs[1].trim());
                }
            }
        }
        return extMetadata;
    }

    public String getToken() {
        return token;
    }

    public Map<String, String> getExtMetadata() {
        return extMetadata;
    }
}
