package com.edgeMapper.EdgeMapper.service;

import com.edgeMapper.EdgeMapper.model.dto.DeviceDto;
import com.google.gson.JsonObject;

/**
 * Created by huqiaoqian on 2020/9/23
 */
public interface MqttMsgService {
    public void updateDeviceTwin(String deviceName, JsonObject data);

    public void launchOrder(String order);

    public void transferBleGatewayData(String data);

    public void reconnect();

    public void pushDataToTb(DeviceDto deviceDto);
}
