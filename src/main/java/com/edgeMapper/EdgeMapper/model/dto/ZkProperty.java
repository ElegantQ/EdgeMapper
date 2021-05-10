package com.edgeMapper.EdgeMapper.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by huqiaoqian on 2021/05/06
 */
@Data
public class ZkProperty implements Serializable {
    private static final long serialVersionUID = 6201239771753024099L;

    private String expected;

    private String reported;
}
