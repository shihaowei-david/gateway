package person.shw.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import person.shw.gateway.filter.DecoratorFilter;
import person.shw.gateway.filter.InitialFilter;
import person.shw.gateway.filter.support.ApiSignFilter;
import person.shw.gateway.filter.support.TokenFilter;

/**
 * @author shihaowei
 * @date 2020/7/9 4:39 下午
 */
@Configuration(proxyBeanMethods = false)
public class FilterConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public DecoratorFilter decoratorFilter(){
        return new DecoratorFilter();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE+1)
    public InitialFilter webFilter(){
        return new InitialFilter();
    }

    @Bean
    @Order(2)
    public TokenFilter tokenFilter(){
        return new TokenFilter();
    }

    @Bean
    @Order(2)
    public ApiSignFilter apiSignFilter(){
        return new ApiSignFilter();
    }
}
