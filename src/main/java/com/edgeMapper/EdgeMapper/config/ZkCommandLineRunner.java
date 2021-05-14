package com.edgeMapper.EdgeMapper.config;

import com.alibaba.fastjson.JSONObject;
import com.edgeMapper.EdgeMapper.model.dto.ZkPropertyDto;
import com.edgeMapper.EdgeMapper.mqtt.AppMqttManager;
import com.edgeMapper.EdgeMapper.mqtt.EdgeMqttManager;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
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
        runDevice(zkConfig.getNode() + "/device", client);
        runApp(zkConfig.getNode()+"/app", client);
    }

    public void runDevice(String path, CuratorFramework client) throws Exception {
        List<String> childrenKeys = client.getChildren().forPath(path);
        for (String childrenKey : childrenKeys) {
            String deviceToken = new String(client.getData().forPath(path + "/" + childrenKey), Charsets.UTF_8);
            log.info("child device is {}, token is {}",childrenKey, deviceToken);
            EdgeMqttManager.addClient(childrenKey,deviceToken, mqttConfig.getTbServer());
        }
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);

        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                ChildData data = event.getData();
                log.info("data is {}",data);
                if (data == null) {
                    return;
                }
                String path = data.getPath();
                String dataStr = new String(data.getData());
                log.info("path is {}",path);
                switch (event.getType()) {
                    case CHILD_ADDED:
                        log.info("CHILD_ADDED path={}, data={}",path,dataStr);
                        String[] strings = path.split("/");
                        if(strings.length == 3){
                            break;
                        }
                        String deviceID = strings[strings.length-1];
                        EdgeMqttManager.addClient(deviceID, dataStr, mqttConfig.getTbServer());
                        break;
                    default:
                        break;
                }
            }
        });
        pathChildrenCache.start();
    }

    public void runApp(String path, CuratorFramework client) throws Exception{
        List<String> childrenKeys = client.getChildren().forPath(path);
        for (String childrenKey : childrenKeys) {
            String data = new String(client.getData().forPath(path + "/" + childrenKey), Charsets.UTF_8);
            log.info("child app is {}, app properties is {}",childrenKey, data);
            ZkPropertyDto zkPropertyDto = JSONObject.parseObject(data , ZkPropertyDto.class);
            AppMqttManager.appMap.put(childrenKey, zkPropertyDto);
            AppMqttManager.pushMag(childrenKey,zkPropertyDto);
        }
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);

        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                ChildData data = event.getData();
                log.info("data is {}",data);
                if (data == null) {
                    return;
                }
                String path = data.getPath();
                String dataStr = new String(data.getData());
                log.info("path is {}",path);
                String[] strings = path.split("/");
                String appId = strings[strings.length-1];
                switch (event.getType()) {
                    case CHILD_ADDED: case CHILD_UPDATED :
                        log.info("CHILD_ADDED path={}, data={}",path,dataStr);
                        ZkPropertyDto zkPropertyDto = JSONObject.parseObject(dataStr , ZkPropertyDto.class);
                        AppMqttManager.appMap.put(appId, zkPropertyDto);
                        AppMqttManager.pushMag(appId,zkPropertyDto);
                        break;
                    default:
                        break;
                }
            }
        });
        pathChildrenCache.start();
    }
}
