package com.sc.whorl.authorization.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * secure api放行配置
 */
public class SecurityRegistry {

    private static final SecurityRegistry me = new SecurityRegistry();
    private final List<String> defaultExcludePatterns = new ArrayList<>();
    private final List<String> excludePatterns = new ArrayList<>();

    public SecurityRegistry() {
        this.defaultExcludePatterns.add("/actuator/health/**");
        this.defaultExcludePatterns.add("/v2/api-docs/**");
        this.defaultExcludePatterns.add("/v2/api-docs-ext/**");
        this.defaultExcludePatterns.add("/swagger-resources");
        this.defaultExcludePatterns.add("/error/**");
        this.defaultExcludePatterns.add("/webjars/**");
        this.defaultExcludePatterns.add("/swagger-ui.html");
        this.defaultExcludePatterns.add("/doc.html");
    }

    public static SecurityRegistry me() {
        return me;
    }

    /**
     * 设置放行api
     *
     * @param patterns
     *         api配置
     * @return SecureRegistry
     */
    public void excludePathPatterns(String... patterns) {
        excludePathPatterns(Arrays.asList(patterns));
    }

    /**
     * 设置放行api
     *
     * @param patterns
     *         api配置
     * @return SecureRegistry
     */
    public void excludePathPatterns(List<String> patterns) {
        this.excludePatterns.addAll(patterns);
    }

    /**
     * 获取所有放行api
     *
     * @return list
     */
    public List<String> getExcludePatterns() {
        List<String> list = new ArrayList<>();
        list.addAll(defaultExcludePatterns);
        list.addAll(excludePatterns);
        return list;
    }

}
