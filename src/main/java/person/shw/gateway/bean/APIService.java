package person.shw.gateway.bean;

import java.util.List;

/**
 * @author shihaowei
 * @date 2020/7/8 2:03 下午
 */
public class APIService {
    private String appId;
    private String serviceGroup;//dubboGroup
    private String serviceName;
    private String serviceVersion;//dubboVersion
    private Integer retries;
    private Integer timeout;
    private List<APIMethod> methods;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String name) {
        this.serviceName = name;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
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

    public List<APIMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<APIMethod> methods) {
        this.methods = methods;
    }
}
