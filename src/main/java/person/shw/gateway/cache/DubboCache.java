package person.shw.gateway.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.MethodConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.service.GenericService;
import org.jctools.maps.NonBlockingHashMap;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import person.shw.gateway.bean.RouteService;
import person.shw.gateway.config.DubboNacosConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shihaowei
 * @date 2020/7/8 5:43 下午
 */
public class DubboCache {

    private static final Logger LOG = LoggerFactory.getLogger(DubboCache.class);
    private static final Reflect CONSUMED_SERVICES= Reflect.onClass(ApplicationModel.class).field("CONSUMED_SERVICES");
    private static final Cache<String, ReferenceConfig<GenericService>> DUBBO_SERVICE_CACHE = Caffeine.newBuilder()
            .recordStats()
            .expireAfterWrite(1, TimeUnit.DAYS).removalListener((RemovalListener<String, ReferenceConfig<GenericService>>) (key, config, removalCause) -> {
                try {
                    if (config != null) {
                        Reflect.on(config).set("ref", null);
                    }
                    LOG.info("[DubboCache] ReferenceConfig<GenericService> {} remove success", key);
                } catch (Exception e) {
                    LOG.info("[DubboCache] ReferenceConfig<GenericService> {} remove error", key, e);
                }
            })
            .build();

    public static final NonBlockingHashMap<String, AtomicLong> RPC_EXCEPTION_STATS = new NonBlockingHashMap<>();
    public static final NonBlockingHashMap<String, List<MethodConfig>> SERVICE_METHOD_CONFIGS = new NonBlockingHashMap<>();
    private static ApplicationConfig APPLICATION_CONFIG;
    private static ConsumerConfig CONSUMER_CONFIG;

    public static void init(ApplicationConfig applicationConfig,ConsumerConfig consumerConfig){
        APPLICATION_CONFIG = applicationConfig;
        CONSUMER_CONFIG = consumerConfig;
    }

    /**
     * 初始化reference
     * @param rs
     * @return
     */
    public static ReferenceConfig<GenericService> initOrGetReference(RouteService rs) {
        List<MethodConfig> methodConfigs = SERVICE_METHOD_CONFIGS.getOrDefault(rs.getServiceKey(), null);
        return DUBBO_SERVICE_CACHE.get(rs.getServiceKey(), c -> {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setApplication(APPLICATION_CONFIG);
            reference.setConsumer(CONSUMER_CONFIG);
            reference.setGroup(rs.getServiceGroup());
            reference.setVersion(rs.getServiceVersion());
            reference.setInterface(rs.getServiceName());
            reference.setGeneric("true");
            reference.setCheck(false);
            Map<String, String> parameters  = new HashMap<>();
//            parameters.put("connections","1");
            reference.setParameters(parameters);

            Integer serviceRetries = rs.getRetries();
            if (serviceRetries != null && serviceRetries > 0) {
                reference.setRetries(serviceRetries);
            } else {
                reference.setRetries(DubboNacosConfig.CONSUMER_RETRIES);
            }
            Integer serviceTimeout = rs.getTimeout();
            if (serviceTimeout != null && serviceTimeout > 0) {
                reference.setTimeout(serviceTimeout);
            } else {
                reference.setTimeout(DubboNacosConfig.CONSUMER_TIMEOUT);
            }

            if (methodConfigs != null) {
                reference.setMethods(methodConfigs);
            }
            return reference;
        });
    }

    public static void invalidOne(RouteService rs) {
        CONSUMED_SERVICES.call("remove", rs.getServiceKey());
        DUBBO_SERVICE_CACHE.invalidate(rs.getServiceKey());
    }
}
