package com.edgeMapper.EdgeMapper.mqtt;

import com.edgeMapper.EdgeMapper.config.Constants;
import com.edgeMapper.EdgeMapper.config.MqttConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by huqiaoqian on 2020/9/23
 */
@Slf4j
@Configuration
public class EdgeMqttClient {

    @Resource
    private MqttConfig mqttConfig;

    @Resource(name="MqttClient")
    private MqttClient mqttClient;

    @Resource(name="TbMqttClient")
    private MqttClient tbMqttClient;

    @Autowired
    private EdgeMqttCallback edgeMqttCallback;

//    @Bean
//    public MqttClient defaultMqttClient() throws MqttException {
//        return new MqttClient(mqttConfig.getServer(), mqttConfig.getClientId(),new MemoryPersistence());
//    }
    private MqttConnectOptions getOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(mqttConfig.isCleanSession());
        options.setConnectionTimeout(mqttConfig.getConnectionTimeout());
        options.setKeepAliveInterval(mqttConfig.getKeepAliveInterval());
        return options;
    }

    private MqttConnectOptions getTbOptions(){
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(mqttConfig.isCleanSession());
        options.setConnectionTimeout(mqttConfig.getConnectionTimeout());
        options.setKeepAliveInterval(mqttConfig.getKeepAliveInterval());
        options.setUserName(Constants.BleWatchToken);
        return options;
    }

    @PostConstruct
    public void init() throws MqttException {
        String[] topics = new String[]{Constants.DeviceETPrefix + "+" + Constants.TwinETUpdateSuffix + "/+", Constants.BleGateWayTopic};
        String[] tbTopics=new String[]{Constants.TbTelemetryTopic};
        int[] qoss = new int[]{0,0};
        int[] tbQos= new int[]{0};
        mqttClient.setCallback(edgeMqttCallback);
        mqttClient.connect(getOptions());
        mqttClient.subscribe(topics,qoss);
        tbMqttClient.setCallback(edgeMqttCallback);
        tbMqttClient.connect(getTbOptions());
        tbMqttClient.subscribe(tbTopics,tbQos);
        if(tbMqttClient.isConnected()){
            log.info("ThingsBoardMqtt连接成功");
        }
        if (mqttClient.isConnected()) {
            log.info("Mosquitto连接成功");
        }
    }

}
