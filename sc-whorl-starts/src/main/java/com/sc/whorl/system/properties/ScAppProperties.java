package com.sc.whorl.system.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@ConfigurationProperties(prefix = "whorl.app", ignoreInvalidFields = true)
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

    /**
     * swagger扫描的包,默认com.sc
     */
    private String swaggerBasePackage = "com.sc";


}
