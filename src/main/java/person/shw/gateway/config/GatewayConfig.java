package person.shw.gateway.config;

import org.apache.commons.lang3.StringUtils;
import org.jctools.maps.NonBlockingHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.WebHandler;
import person.shw.gateway.handler.GatewayDispatcherHandler;

/**
 * @author shihaowei
 * @date 2020/6/22 5:44 下午
 */
@EnableWebFlux
@Configuration(proxyBeanMethods = false)
public class GatewayConfig {

   private static final Logger LOG = LoggerFactory.getLogger(GatewayConfig.class);

   public static final NonBlockingHashSet<String> APP_ROUTE = new NonBlockingHashSet<String>();


   @Bean(name = "webHandler")
   public WebHandler webHandler(){
      return new GatewayDispatcherHandler();
   }

   @Value("${gateway.routes:my-app:/api/myapp/v1}")
   public void setAppRoutes(String routes){
      if (StringUtils.isBlank(routes)) {
         return;
      }
      APP_ROUTE.add(routes);
   }

}
