package com.edgeMapper.EdgeMapper.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EdgeMqttManager {

    public static ConcurrentHashMap<String, MqttClient> clientMap = new ConcurrentHashMap<>();

    public static void addClient(String deviceId, String deviceToken, String tbServer) {
        if (!clientMap.containsKey(deviceId)) {
            try {
                MqttClient client = new MqttClient(tbServer, deviceToken);
                client.setCallback(new EdgeMqttHandler(client,deviceToken));
                clientInit(client,deviceToken);
                clientMap.put(deviceId,client);
            } catch (MqttException e) {
                log.info("Create mqtt client failed!");
                e.printStackTrace();
            }
        }
    }

    public static void clientInit(MqttClient client, String deviceToken) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setConnectionTimeout(50);
        options.setKeepAliveInterval(30);
        options.setUserName(deviceToken);
        client.connect(options);
        if(client.isConnected()){
            log.info("ThingsBoardMqtt连接成功, token = {}",deviceToken);
        }
    }

    public static MqttClient getClient(String deviceId) {
        return clientMap.getOrDefault(deviceId, null);
    }
}
