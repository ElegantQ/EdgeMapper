package com.edgeMapper.EdgeMapper.testController;

import com.edgeMapper.EdgeMapper.model.DeviceDto;
import com.edgeMapper.EdgeMapper.service.DeviceDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by huqiaoqian on 2020/9/23
 */
@Api
@RestController
@RequestMapping("mock")
public class testController {

    @Autowired
    DeviceDataService deviceDataService;

    @ApiOperation(value = "给mapper传送数据")
    @PostMapping("data")
    public void mockData(@RequestBody DeviceDto deviceDto){
        deviceDataService.processMsg(deviceDto);
    }

}
