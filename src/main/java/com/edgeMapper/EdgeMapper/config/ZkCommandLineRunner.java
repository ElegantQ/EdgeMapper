package com.edgeMapper.EdgeMapper.config;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ZkCommandLineRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("182.92.222.53:2181",
                5000,1000,retryPolicy);
        client.start();
        List<String> childrenKeys = client.getChildren().forPath("/edge_node/node1");
        for (String childrenKey : childrenKeys) {
            String deviceToken = new String(client.getData().forPath("/edge_node/node1/" + childrenKey), Charsets.UTF_8);
            log.info("child device is {}, token is {}",childrenKey, deviceToken);
        }
        TreeCache treeCache = new TreeCache(client, "/edge_node/node1");

        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                ChildData data = event.getData();
                log.info("data is {}",data);
                if (data == null) {
                    return;
                }
                String path = data.getPath();
                String dataStr = new String(data.getData());
                log.info("path is {}",path);
                switch (event.getType()) {
                    case NODE_ADDED:
                        log.info("CHILD_ADDED path={}, data={}",path,dataStr);
                        break;
                    default:
                        break;
                }
            }
        });
        treeCache.start();
    }
}
