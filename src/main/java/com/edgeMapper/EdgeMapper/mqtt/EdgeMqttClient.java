package com.edgeMapper.EdgeMapper.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by huqiaoqian on 2020/9/23
 */
@Slf4j
@Configuration
public class EdgeMqttClient {

    @Autowired
    private MqttClient mqttClient;
    @Autowired
    private MqttMessage message;
    @Autowired
    private MqttConnectOptions options;
    @Autowired
    private MqttTopic clientTopic;
    @Autowired
    private MqttTopic serverTopic;
    //定义主题，document为云端反馈的主题；update为边缘向云端推送的主题。temperature3为设备名称，其他都固定。
    private static String clientTopicStr ="$hw/events/device/led-light/twin/update/document";
    private static String serverTopicStr ="$hw/events/device/led-light/twin/update";
    private static final String url ="tcp://0.0.0.0:1883";

    //打包为镜像部署，所有需要配置边缘节点的用户及密码
    private static final String userName ="root";
    private static final String password ="123456";

    private ScheduledExecutorService scheduler;

    /**
     * 初始化
     */
    public void start() {
        try {
            // host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            mqttClient = new MqttClient(url, "KubeEdgeClient", new MemoryPersistence());
            // MQTT的连接设置
            options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(userName);
            // 设置连接的密码
            options.setPassword(password.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置回调
            mqttClient.setCallback(new EdgeMqttCallback());
            clientTopic = mqttClient.getTopic(clientTopicStr);
            serverTopic = mqttClient.getTopic(serverTopicStr);
            //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
//            options.setWill(clientTopoc, "close".getBytes(), 2, true);
            mqttClient.connect(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅主题消息
     */
    public void listerData(){
        //订阅消息
        int[] Qos  = {1};
        String[] topic1 = {clientTopicStr};
        try {
            mqttClient.subscribe(topic1, Qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * push 消息到主题
     * @param topic
     * @param message
     * @throws MqttPersistenceException
     * @throws MqttException
     */
    public void publish(MqttTopic topic , MqttMessage message) throws MqttPersistenceException,
            MqttException {
        MqttDeliveryToken token = topic.publish(message);
        token.waitForCompletion();
//        System.out.println("message is published completely! "
//                + token.isComplete());
    }

    /**
     * 发送消息
     * @param deviceInfo
     */
    public void putData(String deviceInfo){
        message = new MqttMessage();
        message.setQos(2);
        message.setRetained(true);
        message.setPayload(deviceInfo.getBytes());
        try {
            publish(serverTopic,message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
