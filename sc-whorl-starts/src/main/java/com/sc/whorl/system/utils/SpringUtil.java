package com.sc.whorl.system.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    public static void setStaticApplicationContext(ApplicationContext context) {
        SpringUtil.context = context;
    }

    public static <T> T getBean(Class<T> clazz) {
        try {
            return context.getBean(clazz);
        } catch (Exception e) {
            log.debug("can not fetch the assign bean by class!", e);
        }
        return null;
    }

    public static <T> T getBean(String beanId) {
        try {
            return (T) context.getBean(beanId);
        } catch (Exception e) {
            log.debug("can not fetch the assign bean by beanId!", e);
        }
        return null;
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        if (clazz == null || null == beanName || "".equals(beanName.trim())) {
            return null;
        }
        try {
            return (T) context.getBean(beanName, clazz);
        } catch (Exception e) {
            log.debug("can not fetch the assign bean by beanId!", e);
        }
        return null;
    }

    public static ApplicationContext getContext() {
        if (context == null) {
            return null;
        }
        return context;
    }

    public static void publishEvent(ApplicationEvent event) {
        if (context == null) {
            return;
        }
        try {
            context.publishEvent(event);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        setStaticApplicationContext(context);
    }


}
