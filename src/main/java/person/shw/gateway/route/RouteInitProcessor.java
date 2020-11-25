package person.shw.gateway.route;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import io.netty.util.HashedWheelTimer;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.MethodConfig;
import org.jctools.maps.NonBlockingHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import person.shw.gateway.bean.*;
import person.shw.gateway.cache.DubboCache;
import person.shw.gateway.cache.RouteCache;
import person.shw.gateway.config.GatewayConfig;
import person.shw.gateway.constant.RequestMethod;
import person.shw.gateway.dubbo.DubboInvoker;
import person.shw.gateway.util.NacosUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shihaowei
 * @date 2020/7/8 11:42 上午
 */
@Component
public class RouteInitProcessor implements BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RouteInitProcessor.class);

    private static final String NACOS_GROUP = "DEFAULT_GROUP";
    private static final HashedWheelTimer WHEEL_TIMER = new HashedWheelTimer();
    /**所有rpc的sevice集合*/
    private static final NonBlockingHashMap<String, Listener> NACOS_APP_CONFIG_LISTENERS = new NonBlockingHashMap<String, Listener>();
    //private static final NonBlockingHashMap<String, Listener> NACOS_SERVICE_CONFIG_LISTENERS = new NonBlockingHashMap<String, Listener>();

    @NacosInjected(properties = @NacosProperties(serverAddr = "120.79.76.230:8848"))
    private ConfigService configService;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equalsIgnoreCase("dubboInvoker")){
            initRoute();
        }
        return bean;
    }

    /** 初始化路由 */
    public void initRoute(){
        String serverStatus = configService.getServerStatus();
        if (!"up".equalsIgnoreCase(serverStatus)){
            LOG.error("[GateWayAPI] could not subscribe gateway api (reason: nacos down)");
        }
        ArrayList<String> list = new ArrayList<String>(GatewayConfig.APP_ROUTE);
        for (String s : list) {
            String[] sps = StringUtils.split(s.trim(), ":");
            if (sps.length !=2){
                continue;
            }

            String appId = sps[0];
            String route = sps[1];
            String appNacosDataId = NacosUtils.getNacosDataId(appId, route);

            // 处理app的配置监听器
            clearAndRegisterAppConfigListener(appNacosDataId);
        }
    }

    /** app的配置监听 */
    private void clearAndRegisterAppConfigListener(final String appNacosDataId){
        Listener oldListener = NACOS_APP_CONFIG_LISTENERS.get(appNacosDataId);
        if (oldListener != null) {
            configService.removeListener(appNacosDataId,NACOS_GROUP,oldListener);
        }
        Listener newListener = new AbstractListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                handleAppConfig(appNacosDataId,configInfo);
            }
        };

        try {
            //String configInfo = configService.getConfigAndSignListener(appNacosDataId, NACOS_GROUP, 5000L, newListener);
            String configInfo = configService.getConfig(appNacosDataId, NACOS_GROUP, 5000L);
            configService.addListener(appNacosDataId, NACOS_GROUP,newListener);
            handleAppConfig(appNacosDataId,configInfo);
            NACOS_APP_CONFIG_LISTENERS.put(appNacosDataId,newListener);

        }catch (Exception e){
            LOG.error("[GatewayAPI] could not get config and add listener of APIAPPlication（dataId={})", appNacosDataId);
        }
    }

    /** 每一个service的配置监听 */
    private void handleAppConfig(String appNacosDataId,String configInfo){
        if (StringUtils.isBlank(configInfo)) {
            return;
        }

        APIApplication apiApplication = JSON.parseObject(configInfo,APIApplication.class);
        if (apiApplication == null) {
            LOG.error("[GatewayAPI] could not resolve config as APIApplication（dataId={})", appNacosDataId);
            return;
        }
        // 清理路由
        Set<String> oldKeys = RouteCache.ROUTE_MAP.keySet();
        for (String oldKey : oldKeys) {
            if (StringUtils.startsWith(oldKey,apiApplication.getRoute()+"/")){
                RouteCache.ROUTE_MAP.remove(oldKey);
            }
        }

        // 重新注册路由
        Map<String,String> serviceDataIdMap = apiApplication.getServiceDataIdMap();
        serviceDataIdMap.forEach((k,v) ->{
            //clearAndRegisterServiceConfigListener(apiApplication, k);
            final String serviceNacosDataId = k;
            try {
                String serviceConfig = configService.getConfig(serviceNacosDataId, NACOS_GROUP, 5000L);
                handleServiceConfig(apiApplication,serviceNacosDataId,serviceConfig);
            }catch (NacosException e){
                LOG.error("[GatewayAPI] could not resolve config as APIService（dataId={})", serviceNacosDataId);
            }
        });
        LOG.info("[GatewayAPI] registed APIApplication（dataId={})", appNacosDataId);

        WHEEL_TIMER.newTimeout(timeout -> clearUnavailableInvoker(),1L, TimeUnit.MINUTES);
        WHEEL_TIMER.newTimeout(timeout -> clearUnavailableInvoker(),3L, TimeUnit.MINUTES);
        WHEEL_TIMER.newTimeout(timeout -> clearUnavailableInvoker(),6L, TimeUnit.MINUTES);

    }

    private void clearUnavailableInvoker(){
        LOG.info("[GatewayDubbo] try to clear unavailable invoker.");
        DubboInvoker.clearUnavailableInvoker(null);
    }

    /*private void clearAndRegisterServiceConfigListener(APIApplication apiApplication,String serviceNacosDataId){
        Listener oldListener = NACOS_SERVICE_CONFIG_LISTENERS.get(serviceNacosDataId);
        if (oldListener != null) {
            configService.removeListener(serviceNacosDataId,NACOS_GROUP,oldListener);
        }
        
        Listener newListener = new AbstractListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                handleServiceConfig(apiApplication,serviceNacosDataId,configInfo);
            }
        };

        try {
            //String contentInfo = configService.getConfigAndSignListener(serviceNacosDataId, NACOS_GROUP, 5000, newListener);
            String configInfo = configService.getConfig(serviceNacosDataId, NACOS_GROUP, 5000);
            configService.addListener(serviceNacosDataId, NACOS_GROUP,newListener);
            handleServiceConfig(apiApplication,serviceNacosDataId,configInfo);
            NACOS_SERVICE_CONFIG_LISTENERS.put(serviceNacosDataId,newListener);
        }catch (Exception e){
            LOG.error("[GatewayAPI] could not get config and add listener of APIService（dataId={})", serviceNacosDataId);
        }

    }*/

    private void handleServiceConfig(APIApplication apiApplication, String serviceNacosDataId, String configInfo) {
        APIService apiService = JSON.parseObject(configInfo,APIService.class);

        String serviceGroup = apiService.getServiceGroup();
        String serviceName = apiService.getServiceName();
        String serviceVersion = apiService.getServiceVersion();

        RouteService routeService = new RouteService(serviceGroup,serviceName,serviceVersion);
        Integer serviceRetries = apiService.getRetries();
        Integer serviceTimeout = apiService.getTimeout();
        if (serviceRetries != null && serviceRetries > 0){
            routeService.setRetries(serviceRetries);
        }
        if (serviceTimeout != null && serviceTimeout > 0){
            routeService.setTimeout(serviceTimeout);
        }

        List<APIMethod> methods = apiService.getMethods();
        List<MethodConfig> methodConfigs = new ArrayList<>();
        for (APIMethod method : methods) {
            String fullPath = apiApplication.getRoute() + method.getPath();

            RouteMethod routeMethod = new RouteMethod();
            routeMethod.setFullPath(fullPath);
            routeMethod.setRouteService(routeService);

            routeMethod.setMethodName(method.getMethodName());
            if (RequestMethod.GET.name().equalsIgnoreCase(method.getRequestMethod())){
                routeMethod.setRequestMethod(RequestMethod.GET);
            }else {
                routeMethod.setRequestMethod(RequestMethod.POST);
            }

            routeMethod.setParamNames(method.getParamNames());
            routeMethod.setParamTypes(method.getParamTypes());
            routeMethod.setReturnType(method.getReturnType());

            Integer retries = method.getRetries();
            Integer timeout = method.getTimeout();

            MethodConfig methodConfig = new MethodConfig();
            methodConfig.setName(method.getMethodName());
            if (retries != null && retries > 0) {
                routeMethod.setRetries(retries);
                methodConfig.setRetries(retries);
            }
//            else {
//                rm.setRetries(-1);
//                methodConfig.setRetries(-1);
//            }
            if (timeout != null && timeout > 0) {
                routeMethod.setTimeout(timeout);
                methodConfig.setTimeout(timeout);
            }
//            else {
//                rm.setTimeout(API_TIMEOUT);
//                methodConfig.setTimeout(API_TIMEOUT);
//            }
            methodConfigs.add(methodConfig);

            String routeKey = fullPath + ":" + routeMethod.getRequestMethod().name();
            System.err.println(routeKey);
            RouteCache.ROUTE_MAP.put(routeKey, routeMethod);
        }

        DubboCache.SERVICE_METHOD_CONFIGS.put(routeService.getServiceKey(),methodConfigs);
        DubboCache.RPC_EXCEPTION_STATS.put(routeService.getServiceKey(),new AtomicLong(0));

        try {
            DubboCache.initOrGetReference(routeService).get();
        } catch (Exception e) {
            LOG.error("[GatewayAPI] init dubbo reference failed（dataId={})", serviceNacosDataId, e);
        }

        LOG.info("[GatewayAPI] success to registed APIService（dataId={})", serviceNacosDataId);
    }
}

