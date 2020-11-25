package person.shw.gateway.cache;

import org.jctools.maps.NonBlockingHashMap;
import person.shw.gateway.bean.RouteMethod;

/**
 * @author shihaowei
 * @date 2020/7/8 5:36 下午
 */
public class RouteCache {

    public static final NonBlockingHashMap<String, RouteMethod> ROUTE_MAP = new NonBlockingHashMap<>();

}
