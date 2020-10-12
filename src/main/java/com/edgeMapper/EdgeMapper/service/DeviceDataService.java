package com.edgeMapper.EdgeMapper.service;

import com.edgeMapper.EdgeMapper.model.DeviceDto;

/**
 * Created by huqiaoqian on 2020/9/23
 */
public interface DeviceDataService {
    public void processMsg(DeviceDto deviceDto);

    public void receiveData(byte[] data);
}
