package pl.elite.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "curator")
class CuratorProperties {
    private String url;
    private int sessionTimeout;
    private int connectionTimeout;
    private String retryPolicy;
    private int backoffSleep;
    private int maxSleepTime;

    public String getUrl() {
        return url;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public String getRetryPolicy() {
        return retryPolicy;
    }

    public int getBackoffSleep() {
        return backoffSleep;
    }

    public int getMaxSleepTime() {
        return maxSleepTime;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setRetryPolicy(String retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public void setBackoffSleep(int backoffSleep) {
        this.backoffSleep = backoffSleep;
    }

    public void setMaxSleepTime(int maxSleepTime) {
        this.maxSleepTime = maxSleepTime;
    }
}
