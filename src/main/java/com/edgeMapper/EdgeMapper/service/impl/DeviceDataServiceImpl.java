package com.edgeMapper.EdgeMapper.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edgeMapper.EdgeMapper.config.GatewayConfig;
import com.edgeMapper.EdgeMapper.model.dto.*;
import com.edgeMapper.EdgeMapper.service.DeviceDataService;
import com.edgeMapper.EdgeMapper.service.MqttMsgService;
import com.edgeMapper.EdgeMapper.util.ByteUtil;
import com.edgeMapper.EdgeMapper.util.GatewayUtil;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by huqiaoqian on 2020/9/23
 */
@Slf4j
@Service
public class DeviceDataServiceImpl implements DeviceDataService {
//
//    @Autowired
//    private MqttMsgService mqttService;

    @Autowired
    private DefaultMQProducer producer;

    @Autowired
    private GatewayConfig gatewayConfig;

    @Autowired
    private MqttMsgService mqttMsgService;

    @Override
    public void processMsg(DeviceDto deviceDto) {
        try{
            JsonObject data = new JsonObject();
            for(Map.Entry<String ,String> entry:deviceDto.getProperties().entrySet()){
                data.addProperty(entry.getKey(),entry.getValue());
            }
            Message msg = new Message("device-data", JSONObject.toJSONString(deviceDto).getBytes());
            producer.send(msg);
//            mqttService.updateDeviceTwin(deviceDto.getDeviceName(), data);
        }
        catch (Exception e){
            log.error("推送mq实时数据异常",e);
        }
    }

    @Override
    public void processMsg(byte[] bytes) {
        byte response = bytes[0];
        log.info("收到消息={}", ByteUtil.bytesToHexString(bytes));
        switch (response) {
            case 0x70:
                String clusterId = GatewayUtil.byte2HexStr(Arrays.copyOfRange(bytes, 5, 7));
                log.info("clusterId is {}",clusterId);
                boolean isAlarm=false;
                JsonObject data = new JsonObject();
                DeviceDataDto deviceDataDto=new DeviceDataDto();
                List<SingleDataDto> dataDtos=new ArrayList<>();
                switch (clusterId) {
                    case "0004":
                        for (int i = 0; i < Integer.parseInt(String.valueOf(bytes[7])); i++) {
                            if (GatewayUtil.byte2HexStr(Arrays.copyOfRange(bytes, 8 + i * 5, 10 + i * 5)).equals("0000")) {
                                if (bytes[10 + i * 5] == 0x21) {
                                    SingleDataDto dataDto = new SingleDataDto();
                                    int illumination = GatewayUtil.dataBytesToInt(Arrays.copyOfRange(bytes, 11 + i * 5, 13 + i * 5));
                                    if (illumination >= 500) {
                                        isAlarm = true;
                                    }
                                    data.addProperty("illumination", String.valueOf(illumination));
                                    dataDto.setName("illumination");
                                    dataDto.setValue(String.valueOf(illumination));
                                    dataDtos.add(dataDto);
                                }
                            }
                        }
                        if (gatewayConfig.getDevices().containsKey("0004")) {
                            String deviceName = gatewayConfig.getDevices().get("0004");
                            deviceDataDto.setDeviceName(deviceName);
                            deviceDataDto.setDataDtos(dataDtos);
                            log.info("设备数据为{}",deviceDataDto);
                            try {
                                Message msg = new Message("device-data", JSONObject.toJSONString(deviceDataDto).getBytes());
                                producer.send(msg);
                            } catch (Exception e) {
                                log.error("推送mq实时数据异常",e);
                            }
                            //todo:光感度过高，上报给云端
//                            if (isAlarm) {
//                                mqttService.updateDeviceTwin(deviceName, data);
//                            }
                        } else {
                            log.error("云端不存在此设备，或是设备名不匹配");
                        }
                        break;
                    default:break;
                }
            case 0x01:
                log.info("全部设备信息={}",bytes);
                break;
            default:
                log.info("消息类型无匹配");
                break;
        }
    }

    @Override
    public void receiveData(byte[] data) {
        DeviceDto deviceDto=new DeviceDto();
    }

    @Override
    public void getBleWatchPower() {
        String order = "680300006B16";
        mqttMsgService.launchOrder(order);
    }

    @Override
    public void setWalkCounts(BodyInfoDto bodyInfoDto) {
        String bodyInfo=getBodyInfo(bodyInfoDto);
        String cs=getCS("68040400"+bodyInfo);
        String order="68040400"+bodyInfo+cs+"16";
        mqttMsgService.launchOrder(order);
    }
    private void setCallDelayTime(Map<String,String> params){
        if(params==null)return;
        if(params.containsKey("delayTime")){
            String time=Integer.valueOf(params.get("delayTime"))/16+""+Integer.valueOf(params.get("delayTime"))%16;
            String cs=getCS("68120200"+time+"01");
            String oreder="68120200"+time+"01"+cs+"16";
            mqttMsgService.launchOrder(oreder);
        }
    }
    //获取校验码
    private String getCS(String s){
        if(s.length()%2==1||s.length()<2){
            log.error("输入字符串长度有误");
            return "";
        }
        int carry=0;
        StringBuffer ans=new StringBuffer();
        for(int i=0;i<s.length();i+=2){
            if(ans.length()==0){
                ans.append(s.substring(0,2));
            }
            else{
                int sum1=ans.charAt(1)-'0'+s.charAt(i+1)-'0';
                carry=sum1/16;
                ans.setCharAt(1,(char)(sum1%16+'0'));
                int sum2=ans.charAt(0)-'0'+s.charAt(i)-'0'+carry;
                ans.setCharAt(0,(char)(sum2%16+'0'));
            }
        }
        return ans.toString();
    }
    private void setWalkCounts(Map<String,String> params){
        BodyInfoDto bodyInfoDto=new BodyInfoDto();
        if(params==null)return;
        for(Map.Entry<String,String> entry:params.entrySet()){
            switch (entry.getKey()){
                case "height":bodyInfoDto.setHeight(Integer.valueOf(entry.getValue()));
                break;
                case "weight":bodyInfoDto.setWeight(Integer.valueOf(entry.getValue()));
                break;
                case "sex":bodyInfoDto.setSex(Integer.valueOf(entry.getValue()));
                break;
                case "age":bodyInfoDto.setAge(Integer.valueOf(entry.getValue()));
                break;
                default:break;
            }
        }
        setWalkCounts(bodyInfoDto);
    }

    @Override
    public void getHeartBeats() {
        String order="68060100006F16";
        mqttMsgService.launchOrder(order);
    }

    @Override
    public void openHeartBeatsTest() {
        String order="68060100017016";
        mqttMsgService.launchOrder(order);
    }

    @Override
    public void handleOrder(OrderDto orderDto) {
        String deviceName=orderDto.getDeviceName();
        String order=orderDto.getAction();
        Map<String,String> params= orderDto.getParams();
        switch (deviceName){
            case "ble-watch":
                if(order!=null){
                    switch (order){
                        case "openHeartBeatsTest":mqttMsgService.launchOrder("68060100017016");
                            break;
                        case "openFatigueTest":mqttMsgService.launchOrder("680a0100017416");
                            break;
                        case "open blood pressure connect":mqttMsgService.launchOrder("682a040001019816");
                            break;
                        case "setCallDelayTime":setCallDelayTime(params);
                        case "setWalkCounts":setWalkCounts(params);
                        default:break;
                    }
                }
            break;
            default:break;
        }
    }

    @Override
    public void closeHeartBeatsTest() {
        String order="68060100027116";
        mqttMsgService.launchOrder(order);
    }

    @Override
    public void getVersion() {
        String order="680700006F16";
        mqttMsgService.launchOrder(order);
    }

    public void updateBleWatchPower() {

    }

    public String getBodyInfo(BodyInfoDto bodyInfoDto){
        String ans="";
        int heights=bodyInfoDto.getHeight();
        int weight=bodyInfoDto.getWeight();
        int age=bodyInfoDto.getAge();
        ans+=heights/16+""+heights%16;
        ans+=weight/16+""+weight%16;
        ans+=bodyInfoDto.getSex()==1?"01":"00";
        ans+=age/16+""+age%16;
        return ans;
//        return "B23C001C";//身高2+体重2+性别2（男：0，女：1）+年龄4:178cm+60kg+男+28岁
    }

}
