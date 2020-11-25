package person.shw.gateway.bean;

import java.util.Map;

/**
 * @author shihaowei
 * @date 2020/7/8 2:01 下午
 */
public class APIApplication {
    private String appId;
    private String route;
    private Map<String, String> serviceDataIdMap;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Map<String, String> getServiceDataIdMap() {
        return serviceDataIdMap;
    }

    public void setServiceDataIdMap(Map<String, String> serviceDataIdMap) {
        this.serviceDataIdMap = serviceDataIdMap;
    }
}
