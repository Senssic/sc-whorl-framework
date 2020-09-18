package com.sc.whorl.authorization.feign;


import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.collection.ListUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign统一Token拦截器
 */
@Slf4j
public class FeignTokenInterceptor implements RequestInterceptor {

    private static final String FEIGN_HEADER_APPLICATION = "feign-application";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.debug("HttpServletRequest对象为空,无法传递feign的token");
            return;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        requestTemplate.header(FEIGN_HEADER_APPLICATION, System.getProperties().getProperty("spring.application.name"));
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (ListUtil.toList("user-agent", "auth", "cookie").contains(name)) {
                    Enumeration<String> values = request.getHeaders(name);
                    ArrayList<String> arrayList = new ArrayList<>();
                    while (values.hasMoreElements()) {
                        arrayList.add(values.nextElement());
                    }
                    requestTemplate.header(name, arrayList);
                }
            }
        }
    }
}
