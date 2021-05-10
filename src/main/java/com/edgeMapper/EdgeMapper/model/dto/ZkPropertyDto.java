package com.edgeMapper.EdgeMapper.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by huqiaoqian on 2021/05/06
 */
@Data
public class ZkPropertyDto implements Serializable {
    private static final long serialVersionUID = 4571076000438224597L;

    private Map<String,ZkProperty> properties;
}
