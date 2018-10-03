package pl.elite.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.concurrent.Executor;

@EnableAutoConfiguration
@Configuration
public class Curator {

    @Bean
    CuratorFramework getCurator(CuratorProperties config) throws InterruptedException {
        RetryPolicy policy = new ExponentialBackoffRetry(config.getBackoffSleep(), config.getMaxSleepTime());
        CuratorFramework client = CuratorFrameworkFactory.newClient(
            config.getUrl(), config.getSessionTimeout(), config.getConnectionTimeout(), policy
        );
        client.start();
        client.blockUntilConnected();

        return client;
    }

    @Bean(name = "watcherExecutor")
    Executor getExecutor() {
        return new SimpleAsyncTaskExecutor("watcher-executor");
    }
}
