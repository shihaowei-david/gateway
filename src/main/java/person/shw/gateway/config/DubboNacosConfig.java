package person.shw.gateway.config;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.annotation.NacosProperty;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import person.shw.gateway.cache.DubboCache;
import person.shw.gateway.dubbo.DubboInvoker;

import javax.annotation.PostConstruct;

/**
 * @author shihaowei
 * @date 2020/6/22 2:51 下午
 */
@Configuration(proxyBeanMethods = false)
@NacosPropertySource(dataId = "register-config",autoRefreshed = true)
// TODO 后期nacos的serverAddr放到apollo配置中心
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "120.79.76.230:8848"))
public class DubboNacosConfig {


    /** 默认组 */
    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    /** 消费者默认重试次数 */
    public static final Integer CONSUMER_RETRIES = -1;
    /** 消费者默认超时（毫秒） */
    public static Integer CONSUMER_TIMEOUT;
    /** 消费者线程数 */
    public static Integer CONSUMER_THREADS;

    @NacosValue(value = "${appname:}",autoRefreshed = true)
    private String appname;
    @NacosValue(value = "${registerUrl:}",autoRefreshed = true)
    private String registerUrl;
    @NacosValue(value = "${username:}",autoRefreshed = true)
    private String username;
    @NacosValue(value = "${password:}",autoRefreshed = true)
    private String password;

    @PostConstruct
    public void init(){
        System.err.println("appname === "+appname);
        System.err.println("registerUrl === "+registerUrl);
        System.err.println("username === "+username);
        System.err.println("password === "+password);
    }

    @Bean
    public ApplicationConfig applicationConfig(){
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(appname);
        return applicationConfig;
    }

    @Bean
    public RegistryConfig registryConfig(){
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registerUrl);
        registryConfig.setUsername(username);
        registryConfig.setPassword(password);
        registryConfig.setCheck(false);
        return registryConfig;
    }

    @Bean
    public ConsumerConfig consumerConfig(RegistryConfig registryConfig){
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setRegistry(registryConfig);
        consumerConfig.setTimeout(5000);
        consumerConfig.setRetries(-1);
        consumerConfig.setThreads(50);
        consumerConfig.setCheck(false);
        return consumerConfig;
    }

    @Bean
    public DubboInvoker dubboInvoker(ApplicationConfig applicationConfig,ConsumerConfig consumerConfig){
        DubboCache.init(applicationConfig,consumerConfig);
        return new DubboInvoker();
    }
}
