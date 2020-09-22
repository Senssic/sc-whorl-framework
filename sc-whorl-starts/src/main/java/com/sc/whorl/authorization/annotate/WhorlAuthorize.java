package com.sc.whorl.authorization.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <一句话功能简述> <功能详细描述>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WhorlAuthorize {
    /**
     * 入参值类似 ADMIN 或 monitor:job:export
     *
     * @return
     */
    String value();
}
