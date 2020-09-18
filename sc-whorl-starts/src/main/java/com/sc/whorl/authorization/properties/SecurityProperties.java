package com.sc.whorl.authorization.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import lombok.Data;

/**
 * jwt相关配置
 *
 *
 */
@Data
@ConfigurationProperties(prefix = "whorl.security", ignoreInvalidFields = true)
public class SecurityProperties {
    /**
     * 是否启动权限认证
     */
    private Boolean enabled;

    /**
     * 权限白名单
     */
    private List<String> whiteUrlList;

    /**
     * 是否启动客户端指纹校验,若使用则更换浏览器或ip导致token校验不通过
     */
    private Boolean fingerprintEnabled = false;

    /**
     * 是否验证拦截url权限
     */
    private Boolean urlAuthorityEnabled = false;

    /**
     * 默认jwt的过期时间,默认一天
     */
    private Long defaultExpiredDate = 86400L;

    /**
     * 默认token存放位置
     */
    private Integer authRedisDbIndex = 3;

}
