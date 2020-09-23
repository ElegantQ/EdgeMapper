package com.edgeMapper.EdgeMapper.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by huqiaoqian on 2020/9/23
 */
@Slf4j
@Component
public class EdgeMqttCallback implements MqttCallback {
    @Autowired
    private EdgeMqttClient mqttClient;

    @Override
    public void connectionLost(Throwable throwable) {
        log.error("connectionLost");
        while (true) {
            try {
                Thread.sleep(1000);
                mqttClient.init();
                break;
            } catch (Exception e) {
                continue;
            }
        }
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        log.info("topic={},message={}",s,mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
