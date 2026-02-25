package com.maxbot.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 火山引擎Ark API配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "volcengine.ark")
public class VolcengineProperties {
    
    private String apiKey;
    private String baseUrl;
    private String imageModel;
    private String videoModel;
}
