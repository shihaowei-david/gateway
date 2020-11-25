package person.shw.gateway.dubbo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.apache.dubbo.rpc.service.GenericException;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import person.shw.gateway.bean.RouteMethod;
import person.shw.gateway.bean.RouteService;
import person.shw.gateway.cache.DubboCache;
import person.shw.gateway.constant.RequestKeyConst;
import person.shw.gateway.decorator.GatewayRequest;
import person.shw.gateway.util.MonoUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shihaowei
 * @date 2020/7/9 11:52 上午
 */
public class DubboInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(DubboInvoker.class);

    public static void clearUnavailableInvoker(RouteService rs){
        DubboProtocol.getDubboProtocol().getInvokers().forEach( s ->{
            boolean shouldDestory = true;
            URL url = s.getUrl();
            String fullServiceName = url.getServiceInterface();
            if (rs != null){
                shouldDestory = fullServiceName.equalsIgnoreCase(rs.getServiceName());
            }
            if (shouldDestory){
                String serviceName = fullServiceName.substring(fullServiceName.lastIndexOf(".") + 1);
                LOG.warn("[GatewayDubbo] try to destroy unavailable invoker.（provider={}:{} service={} url={}）", url.getHost(), url.getPort(), serviceName, url);
                // 清除老地址缓存
                s.destroy();
            }
        });
    }

    public Mono<Void> handleDubboRpc(ServerWebExchange exchange, RouteMethod routeMethod){
        RouteService routeService = routeMethod.getRouteService();
        String methodName = routeMethod.getMethodName();
        ReferenceConfig<GenericService> reference;

        try {
            Pair<String[], Object[]> pair = resolveDubboParam(exchange, routeMethod.getParamTypes());
            String[] paramTypes = pair.getLeft();
            Object[] paramVals = pair.getRight();
            // dubbo泛化调用
            reference = DubboCache.initOrGetReference(routeService);
            GenericService service = reference.get();
            LOG.debug("[dubbo] generic invoke: {}-{}, paramTypes={}, paramVals={}", routeService.getServiceKey(), methodName, paramTypes, paramVals);
            Object obj = service.$invoke(methodName, paramTypes, paramVals);
            return Flux.from(Mono.just(obj)).next().flatMap(r -> MonoUtils.responseJson(exchange, r));
        }catch (JSONException e) {
            return Mono.error(new JSONException("api request param resolve failed"));
        } catch (GenericException e) {
            return Mono.error(new RpcException("api invoke failed (generic)", e));
        } catch (RpcException e) {
            //UNKNOWN_EXCEPTION = 0; 未知类型
            //NETWORK_EXCEPTION = 1; 网络异常
            //TIMEOUT_EXCEPTION = 2; 超时异常
            //BIZ_EXCEPTION = 3;     业务异常
            //FORBIDDEN_EXCEPTION = 4; 服务不可用
            //SERIALIZATION_EXCEPTION = 5; //序列化异常
            //NO_INVOKER_AVAILABLE_AFTER_FILTER = 6; 没有可用的invoker
            AtomicLong stat = DubboCache.RPC_EXCEPTION_STATS.get(routeService.getServiceKey());
            if (e.isNetwork() || e.isForbidded() || e.isNoInvokerAvailableAfterFilter()) {
                stat.set(0);
                LOG.warn("api invoke failed, try to detroy ReferenceConfig of {}", routeService.getServiceKey());
                DubboCache.invalidOne(routeService);
                clearUnavailableInvoker(routeService);
            } else {
                if (stat.incrementAndGet() >= 6) {
                    stat.set(0);
                    LOG.warn("api invoke failed times >= 6, try to detroy ReferenceConfig of {}", routeService.getServiceKey());
                    DubboCache.invalidOne(routeService);
                    clearUnavailableInvoker(routeService);
                }
                if (e.isTimeout()) {
                    return Mono.error(new RpcException(String.format("api invoke timeout: %s - %s", routeService.getServiceKey(), methodName)));
                }
            }

            return Mono.error(new RpcException(String.format("api invoke failed (code=%d)", e.getCode()), e));
        }
    }


    private Pair<String[], Object[]> resolveDubboParam(ServerWebExchange exchange, String[] paramTypes) throws JSONException {
        if (paramTypes == null || paramTypes.length == 0) {
            return new ImmutablePair<>(new String[0], new Object[0]);
        }

        Map<String, Object> paramMap = null;
        GatewayRequest request = (GatewayRequest) exchange.getRequest();
        if (Objects.equals(request.getMethod(), HttpMethod.GET)) {
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            if (queryParams.size() > 0) {
                paramMap = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                    String k = entry.getKey();
                    List<String> v = entry.getValue();
                    if (v != null && v.size() > 0) {
                        paramMap.put(k, v.get(0));
                    }
                }
                //TODO GET做字段类型比对
            }
        } else {
            byte[] bodyBytes = request.getBodyBytes();
            if (bodyBytes == null || bodyBytes.length == 0) {
                paramMap = new HashMap<>();
            } else {
                paramMap = JSON.parseObject(bodyBytes, Map.class);
            }
        }

        if (paramMap == null) {
            paramMap = new HashMap<>();
        }

        Object[] paramVals = new Object[paramTypes.length];

        paramMap.put(RequestKeyConst.REQUEST_TRACE_ID, request.getTraceId());
        //paramMap.put(RequestKeyConst.REQUEST_IP, IpUtils.getIP(request));
        paramMap.put(RequestKeyConst.REQUEST_USER_ID, request.getUserId());
        paramMap.put(RequestKeyConst.REQUEST_TOKEN, request.getToken());
        paramMap.put(RequestKeyConst.EXT_METADATA, request.getExtMetadata());

        paramVals[0] = paramMap;

        return new ImmutablePair<>(paramTypes, paramVals);
    }
}
