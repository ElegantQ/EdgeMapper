package com.edgeMapper.EdgeMapper.manager;

import com.edgeMapper.EdgeMapper.model.DeviceDto;
import com.edgeMapper.EdgeMapper.service.DeviceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by huqiaoqian on 2020/10/12
 */

@Configuration
@EnableScheduling
public class ScheduleTask {
    @Autowired
    @Lazy
    DeviceDataService deviceDataService;
    @Scheduled(fixedRate = 1000 * 10)
    public void send(){
        System.out.println("********定时发送模拟设备数据******");
        Random rand=new Random();
        int i=rand.nextInt(2);
        String status=i==0?"OPEN":"OFF";
        DeviceDto deviceDto=new DeviceDto();
        deviceDto.setDeviceName("led-light-instance-01");
        Map<String,String> properties=new HashMap<>();
        properties.put("power-status",status);
        deviceDto.setProperties(properties);
        deviceDataService.processMsg(deviceDto);
    }
}
