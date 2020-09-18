package com.sc.whorl.authorization.auth;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体
 */
@Getter
@Setter
public class EmbedUser implements Serializable {
    /**
     * 用户编号
     */
    private long userId;
    /**
     * 登陆名称
     */
    private String loginName;
    /**
     * 当前登陆人客户端访问的指纹信息
     */
    private String fingerprint;
    /**
     * 登陆时间
     */
    private Date loginTime;
    /**
     * 角色编号
     */
    private Set<String> roles = new HashSet<>();


    /**
     * 允许的操作例如 monitor:job:list
     */
    private Set<String> permissions = new HashSet<>();

    /**
     * 允许访问的URL
     */
    private Set<String> urls = new HashSet<>();

    /**
     * 允许访问数据列表
     */
    private Set<Long> dataIds = new HashSet<>();

    /**
     * 应用登陆
     */
    private ApplicationEnum application = ApplicationEnum.WEB;


}
