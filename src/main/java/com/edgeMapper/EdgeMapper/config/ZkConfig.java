package com.edgeMapper.EdgeMapper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "zk")
public class ZkConfig {
    private String server;

    private String node;
}
