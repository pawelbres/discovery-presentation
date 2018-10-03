package pl.elite.simple

import org.apache.curator.RetryPolicy
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import spock.lang.Specification

class Test extends Specification {

    def timeout = 1000
    def sleep = 1000
    def sleepMax = 3
    def url = "172.17.0.2:2181/"
    CuratorFramework client

    def "setup"() {
        RetryPolicy policy = new ExponentialBackoffRetry(sleep, sleepMax);
        client = CuratorFrameworkFactory.newClient(url, timeout, timeout, policy);
        client.start();
    }

    def "teardown"() {
        client.close();
    }

    def "test"() {
        given:
            def path = "/test/me/up"
            def children1 = getChildren('/test')
            for (String entry : children1) {
                System.out.println("/" + entry)
            }

        when:
            if (client.checkExists().creatingParentContainersIfNeeded().forPath(path)) {
                System.out.println(path + " exists")
            } else {
                System.out.println(path + " does not exist. Creating...")
                client.create().orSetData().forPath(path, "some data for entry".getBytes())
            }
        then:
            def children2 = getChildren('/test')
            for (String entry : children2) {
                System.out.println("/" + entry)
            }
    }

    def getChildren(path) {
        def children = []
        for (String child: client.getChildren().forPath(path)) {
            def subChildren = getChildren(path + '/' + child)
            if (subChildren.size() > 0) {
                children.addAll(subChildren)
            } else {
                children.add(path + '/' + child)
            }
        }
        return children
    }

}