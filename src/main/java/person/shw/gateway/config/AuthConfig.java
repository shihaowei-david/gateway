package person.shw.gateway.config;

import org.apache.commons.lang3.StringUtils;
import org.jctools.maps.NonBlockingHashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author shihaowei
 * @date 2020/7/9 4:10 下午
 */
@Configuration(proxyBeanMethods = false)
public class AuthConfig {

    public static final NonBlockingHashSet<String> WHITE_LIST = new NonBlockingHashSet<>();

    public static String AUTH_SERVER;

    // TODO 后期配置信息放置apollo
    //@Value("${gateway.auth.server}")
    private void setAuthServer(String val) {
        AUTH_SERVER = val;
    }

    //@Value("${gateway.auth.whiteList}")
    private void setWhiteList(String val) {
        WHITE_LIST.clear();
        if (StringUtils.isBlank(val)) {
            return;
        }

        String[] splits = StringUtils.split(val, ";");
        if (splits == null || splits.length == 0) {
            return;
        }

        for (String s : splits) {
            if (StringUtils.isBlank(s)) {
                continue;
            }
            s = StringUtils.replaceIgnoreCase(s, "\\n", StringUtils.EMPTY);
            s = StringUtils.replaceIgnoreCase(s, "\\r", StringUtils.EMPTY);
            WHITE_LIST.add(s.trim());
        }
    }
}
