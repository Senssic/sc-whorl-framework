package com.sc.whorl.system.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@ConfigurationProperties(prefix = "sc.app", ignoreInvalidFields = true)
public class ScAppProperties {
    /**
     * 应用版本
     */
    private String version;

    /**
     * 应用构建时间
     */
    private String buildTime;

    /**
     * 是否开始防xss攻击
     */
    private Boolean xssFilterEnable=true;


}
