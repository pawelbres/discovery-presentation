package pl.elite.simple;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConfigurationProperties
public class SimpleService {

    private final Logger logger = LoggerFactory.getLogger(SimpleService.class);

    private static final String DISCOVERY_PATH = "/discovery";
    private static final String LOCKS_PATH = "/locks";
    private static final String LEADER_LATCH_PATH = "/leader";
    private static final String TEST_PATH = "/test";

    private final CuratorFramework client;
    private final String uniqId;
    private final Executor executor;

    @Autowired
    public SimpleService(CuratorFramework client, @Value("${curator.uniq.id}") String id, Executor executor) {
        this.client = client;
        this.uniqId = id;
        this.executor = executor;
    }

    @PostConstruct
    public void register() {
        preparePaths(Arrays.asList(DISCOVERY_PATH, LEADER_LATCH_PATH, LOCKS_PATH, TEST_PATH));
        setUpWatcher(TEST_PATH);
        setUpWatcher(DISCOVERY_PATH);
        try {
            client.create()
                .orSetData()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(DISCOVERY_PATH + "/" + uniqId, uniqId.getBytes());
        } catch (Exception e) {
            logger.error("Could not register in discovery.", e);
            throw new RuntimeException(e);
        }
        setupLeadership();
    }

    public List<String> getChildren(String path) {
        try {
            Stream<String> children = client.getChildren().forPath(path).stream()
                .flatMap(child -> getChildren(getPath(path, child)).stream());
            return Stream.concat(Stream.of(path), children).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error cought during reading up info about children.", e);
        }
        logger.info("returning {}", path);
        return Collections.singletonList(path);
    }

    public String get(String path) {
        String result = "Nothing found. Error?";
        try {
            result = new String(client.getData().forPath(path));
        } catch (Exception e) {
            logger.error("Some error occured.", e);
        }

        return result;
    }

    public boolean set(String path, String data) {
        try {
            client.checkExists().creatingParentsIfNeeded().forPath(path);
            client.create().orSetData().forPath(path, data.getBytes());
        } catch (Exception e) {
            logger.error("Some error occured.", e);
            return false;
        }

        return true;
    }

    public boolean setWithACL(String path, String data) {
        try {
            List<ACL> acls = new ArrayList<>();
            acls.add(new ACL(ZooDefs.Perms.ADMIN, new Id()));
            //TODO: check if this String formatting is alright
            acls.add(new ACL(ZooDefs.Perms.READ, new Id("digest", String.format(uniqId, uniqId))));
            client.checkExists().creatingParentsIfNeeded().forPath(path);
            client.create().orSetData().withACL(acls).forPath(path, data.getBytes());
        } catch (Exception e) {
            logger.error("Some error occured.", e);
            return false;
        }

        return true;
    }

    public boolean setWithTTL(String path, String data, long ttlInMilis) {
        try {
            client.checkExists().creatingParentsIfNeeded().forPath(path);
            client.create().orSetData()
                .withTtl(ttlInMilis)
                .withMode(CreateMode.PERSISTENT_WITH_TTL)
                .forPath(path, data.getBytes());
        } catch (Exception e) {
            logger.error("Some error occured while setting up {} with {} for {}.", path, data, ttlInMilis, e);
            return false;
        }

        return true;
    }

    public boolean setWithLock(long lockTime, long waitTime) {
        InterProcessMutex mutex = new InterProcessMutex(client, LOCKS_PATH);
        try {
            mutex.acquire(lockTime, TimeUnit.MILLISECONDS);
            logger.info("[{}] Acquired lock.");
            TimeUnit.MILLISECONDS.sleep(waitTime);
            mutex.release();
            logger.info("[{}] Released lock.");
            return true;
        } catch (Exception e) {
            logger.warn("[{}] Could not acquire lock.", e);
        }
        return false;
    }

    private void setUpWatcher(String path) {
        try {
            client.getChildren().usingWatcher(
                (CuratorWatcher) event ->
                    logger.debug("[{}] Received a notification about some change. Event is: {}", uniqId, event.getType())
            ).forPath(path);
            logger.debug("Watching set up for the '{}'", path);
        } catch (Exception e) {
            logger.error("Exception cought during watch setup.", e);
        }
    }

    private void setupLeadership() {
        LeaderLatch latch = new LeaderLatch(client, LEADER_LATCH_PATH, uniqId, LeaderLatch.CloseMode.NOTIFY_LEADER);
        latch.addListener(
            new LeaderLatchListener() {
                @Override
                public void isLeader() {
                    logger.info("[{}] I'm a leader!!!!", uniqId);
                }

                @Override
                public void notLeader() {
                    logger.info("[{}] I lost leadership!!!", uniqId);
                }
            }
        );
        try {
            latch.start();
        } catch (Exception e) {
            logger.error("Could not startup leadership feature.", e);
        }
    }

    private void preparePaths(List<String> paths) {
        String data = "";
        for (String path : paths) {
            try {
                client.checkExists().creatingParentsIfNeeded().forPath(path);
                client.create().orSetData().forPath(path, data.getBytes());
            } catch (Exception e) {
                logger.error("Could not create preconditions.", e);
                throw new RuntimeException(e);
            }
        }
    }

    private String getPath(String base, String child) {
         return (base + "/" + child).replaceAll("/+", "/");
    }
}
