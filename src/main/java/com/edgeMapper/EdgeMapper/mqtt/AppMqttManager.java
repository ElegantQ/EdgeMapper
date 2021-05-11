package com.edgeMapper.EdgeMapper.mqtt;

import com.edgeMapper.EdgeMapper.model.dto.ZkProperty;
import com.edgeMapper.EdgeMapper.model.dto.ZkPropertyDto;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huqiaoqian on 2021/05/06
 */
@Slf4j
public class AppMqttManager {

    public static ConcurrentHashMap<String, ZkPropertyDto> appMap = new ConcurrentHashMap<>();

    private static MqttClient client;

    static {
        try {
            client = new MqttClient("tcp://124.70.101.24:1883", "app");
            client.setCallback(new EdgeMqttHandler(client,"app"));
            clientInit(client,"app");
        } catch (Exception e) {
            log.info("Create mqtt client failed!");
            e.printStackTrace();
        }

    }

    public static void clientInit(MqttClient client, String clientId) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setConnectionTimeout(50);
        options.setKeepAliveInterval(30);
        options.setUserName(clientId);
        client.connect(options);
        if(client.isConnected()){
            log.info("AppMqttClient 连接成功, clientId = {}",client);
        }
    }

    public static MqttClient getAppClient() {
        return client;
    }

    public static void pushMag(String appId, ZkPropertyDto zkPropertyDto) {
        switch (appId) {
            case "camera_detection":
                Map<String,Integer> map = new HashMap<>();
                ZkProperty deviceId = zkPropertyDto.getProperties().getOrDefault("deviceId",null);
                if (deviceId == null) {
                    map.put("deviceId",0);
                } else {
                    map.put("deviceId",Integer.valueOf(deviceId.getExpected()));
                }
                ZkProperty command = zkPropertyDto.getProperties().getOrDefault("command", null);
                if (command == null) {
                    map.put("command",0);
                } else {
                    map.put("command", Integer.valueOf(command.getExpected()));
                }
                ZkProperty status = zkPropertyDto.getProperties().getOrDefault("status", null);
                if (status == null) {
                    map.put("status",0);
                } else {
                    map.put("status",Integer.valueOf(status.getExpected()));
                }
                MqttMessage msg = new MqttMessage(map.toString().getBytes());
                try {
                    client.publish(appId,msg);
                } catch (Exception e) {
                    log.info("publish msg failed");
                    e.printStackTrace();
                }
                break;
            default:
                msg = new MqttMessage(zkPropertyDto.toString().getBytes());
                try {
                    client.publish(appId,msg);
                } catch (Exception e) {
                    log.info("publish msg failed");
                    e.printStackTrace();
                }
                break;
        }
    }
}
