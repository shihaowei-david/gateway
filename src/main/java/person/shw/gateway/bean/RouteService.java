package person.shw.gateway.bean;

import org.apache.dubbo.common.URL;

/**
 * @author shihaowei
 * @date 2020/6/22 6:24 下午
 */
public class RouteService {

    private String serviceGroup;
    private String serviceVersion;
    private String serviceName;
    private String serviceKey;
    private Integer retries;
    private Integer timeout;


    public RouteService(String serviceGroup, String serviceName, String serviceVersion) {
        this.serviceGroup = serviceGroup;
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.serviceKey = URL.buildKey(serviceName,serviceGroup,serviceVersion);
    }


    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }


    @Override
    public String toString() {
        return "RouteService{" +
                "serviceGroup='" + serviceGroup + '\'' +
                ", serviceVersion='" + serviceVersion + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceKey='" + serviceKey + '\'' +
                ", retries=" + retries +
                ", timeout=" + timeout +
                '}';
    }
}
