package com.example.zookeeper.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

@Slf4j
public class NodeListener implements Watcher {

    @Override
    public void process(WatchedEvent event) {
        log.info("[receive message] Path: {} KeeperState: {} type: {}", event.getPath(), event.getState(), event.getType());
    }
}
