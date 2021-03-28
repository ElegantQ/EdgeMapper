package com.edgeMapper.EdgeMapper.mqtt;

import com.edgeMapper.EdgeMapper.service.MqttMsgService;
import com.edgeMapper.EdgeMapper.service.impl.MqttMsgServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@Slf4j
public class EdgeMqttHandler implements MqttCallback {

    private MqttMsgService mqttMsgService;

    private MqttClient client;

    private String deviceToken;

    public EdgeMqttHandler(MqttClient client, String deviceToken) {
        this.client = client;
        this.deviceToken = deviceToken;
        this.mqttMsgService = new MqttMsgServiceImpl();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        log.error("connectionLost");
        while (true) {
            try {
                Thread.sleep(1000);
                EdgeMqttManager.clientInit(client,deviceToken);
                break;
            } catch (Exception e) {
                e.printStackTrace();
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
