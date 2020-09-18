package com.sc.whorl.authorization.interceptor;


import com.sc.whorl.authorization.auth.EmbedUser;
import com.sc.whorl.authorization.properties.SecurityProperties;
import com.sc.whorl.authorization.utils.SecureUtil;
import com.sc.whorl.contants.WhorlDict;
import com.sc.whorl.system.utils.ScUtils;
import com.sc.whorl.system.utils.SpringUtil;
import com.sc.whorl.system.utils.StringPool;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * jwt拦截器校验
 */
@Slf4j
public class SecurityInterceptor extends HandlerInterceptorAdapter {

    /**
     * 将通配符表达式转化为正则表达式
     *
     * @param path
     * @return
     */
    private static String getRegPath(String path) {
        char[] chars = path.toCharArray();
        int len = chars.length;
        StringBuilder sb = new StringBuilder();
        boolean preX = false;
        for (int i = 0; i < len; i++) {
            if (chars[i] == '*') {
                if (preX) {
                    sb.append(".*");
                    preX = false;
                } else if (i + 1 == len) {
                    sb.append("[^/]*");
                } else {
                    preX = true;
                    continue;
                }
            } else {
                if (preX) {
                    sb.append("[^/]*");
                    preX = false;
                }
                if (chars[i] == '?') {
                    sb.append('.');
                } else {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 通配符模式，若遇到/aa/{bb}/cc或/aa/{bb} 转换为/aa/* /cc和 /aa/*再匹配
     */
    private static boolean match(String pattern, String requestPath) {
        String regPath = getRegPath(pattern.trim().replaceAll("/\\{.+\\}", "/*"));
        return Pattern.compile(regPath).matcher(requestPath).matches();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            return true;
        }
        Integer responseCode = HttpStatus.FORBIDDEN.value();
        boolean exists = false;
        EmbedUser embedUser = null;
        //SecurityWebAutoConfiguration 处已经将白名单去除
        String auth = request.getHeader(WhorlDict.AUTH);
        StringRedisTemplate authRedisTemplate = SpringUtil.getBean(WhorlDict.AUTH_REDIS_TEMPTLATE_BEAN);
        SecurityProperties securityProperties = SpringUtil.getBean(SecurityProperties.class);
        if (!ObjectUtils.isEmpty(auth)) {
            String embUserJson = authRedisTemplate.opsForValue().get(SecureUtil.SECURE_AUTH_USER_PREFIX + auth);
            if (!ObjectUtils.isEmpty(embUserJson)) {
                embedUser = SecureUtil.getUser();
                if (null != embedUser) {
                    if (securityProperties.getUrlAuthorityEnabled()) {
                        String path = request.getRequestURI();
                        for (String pattern : SecureUtil.getUser().getUrls()) {
                            exists = match(pattern, path);
                            if (exists) {
                                break;
                            }
                            //2.若是因为没有URL访问权限则返回码是没有权限
                            responseCode = HttpStatus.UNAUTHORIZED.value();
                        }
                    } else {
                        //不验证url是否拦截,则只要token正确就全部允许通过
                        exists = true;
                    }
                    //校验客户端指纹信息
                    if (securityProperties.getFingerprintEnabled() && !ScUtils.getUserIdentityInfo().equals(embedUser.getFingerprint())) {
                        log.warn("token指纹信息校验失败!{},{}", ScUtils.getUserIdentityInfo(), embedUser.getFingerprint());
                        exists = false;
                    }
                }
            } else {
                log.warn("token is not exists or expired!token:{}", auth);
            }

        }
        if (!exists) {
            log.warn("签名认证失败，请求接口：{}，请求IP：{}，请求参数：{}，请求token:{}，返回码:{}", request.getRequestURI(), ScUtils.getIP(request), JSONUtil.toJsonStr(request.getParameterMap()), auth, responseCode);
            try {
                response.setCharacterEncoding(StringPool.UTF_8);
                response.sendError(responseCode, "主机IP[" + InetAddress.getLocalHost().getHostAddress() + "]拒绝访问，没有访问权限：" + request.getRequestURI());
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
            return false;
        } else {
            if (embedUser != null) {
                SecureUtil.setCurrentEmbedUser(embedUser);
                //刷新token过期时间
                Boolean isExpireUserOk = authRedisTemplate.expire(SecureUtil.SECURE_AUTH_USER_PREFIX + auth, securityProperties.getDefaultExpiredDate(), TimeUnit.SECONDS);
                Boolean isExpireUidOk = authRedisTemplate.expire(String.format(SecureUtil.SECURE_AUTH_UID_PREFIX, embedUser.getApplication()) + embedUser.getUserId(), securityProperties.getDefaultExpiredDate(), TimeUnit.SECONDS);
                Optional<Boolean> usertest = Optional.ofNullable(isExpireUserOk);
                Optional<Boolean> uidtest = Optional.ofNullable(isExpireUidOk);
                if (!usertest.isPresent() || !uidtest.isPresent()) {
                    log.warn("token {} exprie is failed!", auth);
                }
            }
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SecureUtil.clearCurrentEmbedUser();
    }
}
