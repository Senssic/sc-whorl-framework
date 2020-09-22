package com.sc.whorl.authorization.configuration;


import com.sc.whorl.authorization.annotate.PermissionService;
import com.sc.whorl.authorization.annotate.PreAuthorizeAspect;
import com.sc.whorl.authorization.interceptor.SecurityInterceptor;
import com.sc.whorl.authorization.interceptor.TokenArgumentResolver;
import com.sc.whorl.authorization.properties.SecurityProperties;
import com.sc.whorl.authorization.registry.SecurityRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 安全配置类
 */
@Order
@Slf4j
@Configuration
@AllArgsConstructor
@ConditionalOnProperty(value = "whorl.security.enabled", havingValue = "true")
@EnableConfigurationProperties({SecurityProperties.class})
public class SecurityWebAutoConfiguration implements WebMvcConfigurer {

    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private RedisProperties redisProperties;

    /**
     * 若controller中的参数直接有EmbedUser 则直接注入EmbedUser
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new TokenArgumentResolver());
    }

    @Bean("auth")
    public PermissionService permissionService() {
        return new PermissionService();
    }

    @Bean
    public PreAuthorizeAspect preAuthorizeAspect() {
        return new PreAuthorizeAspect();
    }


    /**
     * 认证：拦截及明名单
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> list = securityProperties.getWhiteUrlList();
        if (list != null) {
            String[] arr = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = String.valueOf(list.get(i));
            }
            SecurityRegistry.me().excludePathPatterns(arr);
        }
        registry.addInterceptor(new SecurityInterceptor()).addPathPatterns("/**")
                .excludePathPatterns(SecurityRegistry.me().getExcludePatterns());
    }


    @Bean(name = "authRedisTemplate")
    public RedisTemplate authRedisTemplate() {
        LettuceConnectionFactory lettuceConnectionFactory;
        if (!ObjectUtils.isEmpty(redisProperties.getCluster())) {
            RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
            redisClusterConfiguration.setPassword(redisProperties.getPassword());
            lettuceConnectionFactory = new LettuceConnectionFactory(redisClusterConfiguration);
        } else {
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
            redisStandaloneConfiguration.setDatabase(securityProperties.getAuthRedisDbIndex());
            redisStandaloneConfiguration.setHostName(redisProperties.getHost());
            redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
            redisStandaloneConfiguration.setPort(redisProperties.getPort());
            lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        }
        lettuceConnectionFactory.afterPropertiesSet();
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(lettuceConnectionFactory);
        stringRedisTemplate.afterPropertiesSet();
        return stringRedisTemplate;
    }

}
