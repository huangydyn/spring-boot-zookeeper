package com.example.zookeeper.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@Slf4j
@RestController
public class LockApi {

    private final ZooKeeper zkClient;

    private static CountDownLatch latch;

    private final String path = "/lock";

    public LockApi(ZooKeeper zkClient) {
        this.zkClient = zkClient;
    }

    @RequestMapping("/test")
    public String get() throws Exception {
        tryLock();
        execute();
        unLock();
        return "success";
    }

    private void unLock() {
        try {
            zkClient.delete(path, -1);
        } catch (Exception e) {
            log.error("lock met error: {}", e.getMessage());
            throw new RuntimeException("");
        }
    }

    private void execute() throws InterruptedException {
        log.info("get lock success");
        Thread.sleep(100000);
    }

    private void tryLock() {
        try {
            zkClient.create(path, "1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (KeeperException e) {
            log.info("get lock failed");
            await();
            tryLock();
        } catch (Exception e) {
            log.error("lock met error: {}", e.getMessage());
            throw new RuntimeException("");
        }
    }

    private void await() {
        try {
            latch = new CountDownLatch(1);
            zkClient.addWatch(path, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                    latch.countDown();
                }
            }, AddWatchMode.PERSISTENT);
            latch.await();
        } catch (Exception e) {
            log.error("lock met error: {}", e.getMessage());
        }
    }
}
