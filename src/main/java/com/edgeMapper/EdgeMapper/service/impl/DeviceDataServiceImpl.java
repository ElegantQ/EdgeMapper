package com.edgeMapper.EdgeMapper.service.impl;

import com.edgeMapper.EdgeMapper.model.DeviceDto;
import com.edgeMapper.EdgeMapper.service.DeviceDataService;
import com.edgeMapper.EdgeMapper.service.MqttMsgService;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by huqiaoqian on 2020/9/23
 */
@Slf4j
@Service
public class DeviceDataServiceImpl implements DeviceDataService {

    @Autowired
    private MqttMsgService mqttService;
    @Override
    public void processMsg(DeviceDto deviceDto) {
        try{
            JsonObject data = new JsonObject();
            for(Map.Entry<String ,String> entry:deviceDto.getProperties().entrySet()){
                data.addProperty(entry.getKey(),entry.getValue());
            }
            mqttService.updateDeviceTwin(deviceDto.getDeviceName(), data);
        }
        catch (Exception e){
            log.error("推送mq实时数据异常",e);
        }
    }

    @Override
    public void receiveData(byte[] data) {
        DeviceDto deviceDto=new DeviceDto();
    }
}
