package com.edgeMapper.EdgeMapper.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class OrderDto{
    private String deviceName;
    private String action;
    private Map<String,String> params;
}
