package com.edgeMapper.EdgeMapper.manager;

import com.edgeMapper.EdgeMapper.service.DeviceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by huqiaoqian on 2020/10/12
 */

@Configuration
@EnableScheduling
public class ScheduleTask {
    @Autowired
    @Lazy
    DeviceDataService deviceDataService;
    @Scheduled(fixedRate = 1000 * 3)
    public void send(){
        System.out.println("********定时发送模拟设备数据(ble-watch)******");
//        Random rand=new Random();
//        int i=rand.nextInt(2);
//        String status=i==0?"OPEN":"OFF";
//        DeviceDto deviceDto=new DeviceDto();
//        deviceDto.setDeviceName("led-light-instance-01");
//        Map<String,String> properties=new HashMap<>();
//        properties.put("power-status",status);
//        deviceDto.setProperties(properties);
//        deviceDataService.processMsg(deviceDto);
//        System.out.print("向手环发送查看电量请求");
        deviceDataService.getBleWatchPower();//查看手环电量
        System.out.print("向手环发送获取实时体征数据请求");
        deviceDataService.getHeartBeats();//获取实时心率数据
        deviceDataService.getVersion();//获取版本号
    }
}
