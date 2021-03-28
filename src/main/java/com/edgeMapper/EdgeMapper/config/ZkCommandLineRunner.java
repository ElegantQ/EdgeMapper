package com.edgeMapper.EdgeMapper.config;

import com.edgeMapper.EdgeMapper.mqtt.EdgeMqttManager;
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

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class ZkCommandLineRunner implements CommandLineRunner {
    @Resource
    public MqttConfig mqttConfig;

    @Resource
    public ZkConfig zkConfig;

    @Override
    public void run(String... args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkConfig.getServer(),
                5000,1000,retryPolicy);
        client.start();
        List<String> childrenKeys = client.getChildren().forPath(zkConfig.getNode());
        for (String childrenKey : childrenKeys) {
            String deviceToken = new String(client.getData().forPath(zkConfig.getNode() + "/" + childrenKey), Charsets.UTF_8);
            log.info("child device is {}, token is {}",childrenKey, deviceToken);
            EdgeMqttManager.addClient(childrenKey,deviceToken, mqttConfig.getTbServer());
        }
        TreeCache treeCache = new TreeCache(client, zkConfig.getNode());

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
                        String[] strings = path.split("/");
                        String deviceID = strings[strings.length-1];
                        EdgeMqttManager.addClient(deviceID, dataStr, mqttConfig.getTbServer());
                        break;
                    default:
                        break;
                }
            }
        });
        treeCache.start();
    }
}
