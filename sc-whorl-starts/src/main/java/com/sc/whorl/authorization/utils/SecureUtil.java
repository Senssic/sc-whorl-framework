package com.sc.whorl.authorization.utils;


import com.sc.whorl.authorization.auth.ApplicationEnum;
import com.sc.whorl.authorization.auth.EmbedUser;
import com.sc.whorl.authorization.properties.SecurityProperties;
import com.sc.whorl.contants.WhorlDict;
import com.sc.whorl.system.common.ScException;
import com.sc.whorl.system.utils.ScUtils;
import com.sc.whorl.system.utils.SpringUtil;
import com.sc.whorl.system.utils.StringPool;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ObjectUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Secure工具类
 */
@Slf4j
public class SecureUtil {
    public static final String SECURE_AUTH_USER_PREFIX = "SECURE:USER:";
    public static final String SECURE_AUTH_UID_PREFIX = "SECURE:%s:";

    private static final ThreadLocal<EmbedUser> embedUserThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户缓存
     *
     * @param embedUser
     */
    public static void setCurrentEmbedUser(EmbedUser embedUser) {
        embedUserThreadLocal.set(embedUser);
    }

    /**
     * 清空当前用户缓存
     */
    public static void clearCurrentEmbedUser() {
        embedUserThreadLocal.remove();
    }

    /**
     * 获取用户信息
     */
    public static EmbedUser getUser() {
        EmbedUser embedUser = embedUserThreadLocal.get();
        if (null != embedUser) {
            return embedUser;
        }
        String authToken = ScUtils.getRequest().getHeader(WhorlDict.AUTH);
        try {
            if (!ObjectUtils.isEmpty(authToken)) {
                StringRedisTemplate authRedisTemplate = SpringUtil.getBean(WhorlDict.AUTH_REDIS_TEMPTLATE_BEAN);
                if (null == authRedisTemplate) {
                    return null;
                } else {
                    return JSONUtil.toBean(authRedisTemplate.opsForValue().get(SECURE_AUTH_USER_PREFIX + authToken), EmbedUser.class);
                }
            }
        } catch (Exception e) {
            log.error("parse embed user error!", e);
            return null;
        }
        //throw new RCException("The user token is illegal or does not exist. Please check whether the token is invalid!");
        return null;
    }


    /**
     * 获取用户id
     *
     * @return userId
     */
    public static Long getUserId() {
        return (null == getUser()) ? -1 : getUser().getUserId();
    }


    /**
     * 获取用户账号
     *
     * @return userAccount
     */
    public static String getUserLoginName() {
        return (null == getUser()) ? StringPool.EMPTY : getUser().getLoginName();
    }

    /**
     * 生成jwt给到前端
     *
     * @param embedUser
     * @return
     */
    public static String getJwtToken(EmbedUser embedUser) {
        if (!Optional.ofNullable(embedUser.getApplication()).isPresent()) {
            throw new ScException("embedUser application is not null!");
        }
        StringRedisTemplate authRedisTemplate = SpringUtil.getBean(WhorlDict.AUTH_REDIS_TEMPTLATE_BEAN);
        String jwtToken = UUID.randomUUID().toString(true);
        /**
         * 若已经存在唯一编号则直接使用已经存在的token
         */
        if (authRedisTemplate.hasKey(String.format(SECURE_AUTH_UID_PREFIX, embedUser.getApplication()) + embedUser.getUserId())) {
            jwtToken = authRedisTemplate.opsForValue().get(String.format(SECURE_AUTH_UID_PREFIX, embedUser.getApplication()) + embedUser.getUserId());
        }

        SecurityProperties securityProperties = SpringUtil.getBean(SecurityProperties.class);
        embedUser.setFingerprint(ScUtils.getUserIdentityInfo());
        authRedisTemplate.opsForValue().set(SECURE_AUTH_USER_PREFIX + jwtToken, JSONUtil.toJsonStr(embedUser), securityProperties.getDefaultExpiredDate(), TimeUnit.SECONDS);
        authRedisTemplate.opsForValue().set(String.format(SECURE_AUTH_UID_PREFIX, embedUser.getApplication()) + embedUser.getUserId(), Optional.ofNullable(jwtToken).get(), securityProperties.getDefaultExpiredDate(), TimeUnit.SECONDS);
        return jwtToken;
    }


    /**
     * 销毁当前登陆的用户信息
     *
     * @return
     */
    public static Boolean invalidate() {
        try {
            StringRedisTemplate authRedisTemplate = SpringUtil.getBean(WhorlDict.AUTH_REDIS_TEMPTLATE_BEAN);
            Optional<HttpServletRequest> RequestNotNull = Optional.ofNullable(ScUtils.getRequest());
            String authToken = RequestNotNull.get().getHeader(WhorlDict.AUTH);
            if (authRedisTemplate.hasKey(SECURE_AUTH_USER_PREFIX + authToken)) {
                EmbedUser embedUser = JSONUtil.toBean(authRedisTemplate.opsForValue().get(SECURE_AUTH_USER_PREFIX + authToken), EmbedUser.class);
                authRedisTemplate.delete(SECURE_AUTH_USER_PREFIX + authToken);
                authRedisTemplate.delete(String.format(SECURE_AUTH_UID_PREFIX, embedUser.getApplication()) + embedUser.getUserId());
                log.info("invalidate token {}", authToken);
            }
            clearCurrentEmbedUser();
            return true;
        } catch (Exception e) {
            log.error("failed to destroy user information!", e);
            return false;
        }
    }


    /**
     * 销毁当前登陆的用户信息,根据用户编号
     *
     * @return
     */
    public static Boolean invalidate(ApplicationEnum applicationEnum, long userId) {
        try {
            StringRedisTemplate authRedisTemplate = SpringUtil.getBean(WhorlDict.AUTH_REDIS_TEMPTLATE_BEAN);
            if (authRedisTemplate.hasKey(String.format(SECURE_AUTH_UID_PREFIX, applicationEnum) + userId)) {
                String authToken = authRedisTemplate.opsForValue().get(String.format(SECURE_AUTH_UID_PREFIX, applicationEnum) + userId);
                authRedisTemplate.delete(SECURE_AUTH_USER_PREFIX + authToken);
                authRedisTemplate.delete(String.format(SECURE_AUTH_UID_PREFIX, applicationEnum) + userId);
                log.info("invalidate token {} by userId {}", authToken, userId);
            }
            return true;
        } catch (Exception e) {
            log.error("failed to destroy user information!", e);
            return false;
        }
    }
}
