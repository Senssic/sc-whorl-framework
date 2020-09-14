package com.sc.whorl.system.extend.xss;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.util.StrUtil;


public class XssFilter implements Filter {

	@Override
	public void init(FilterConfig config) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		ServletRequest requestWrapper = null;
		if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            //header非空且为json类型的才处理,不影响其他基于http协议的比如hessian
            String requestHeader = httpServletRequest.getHeader(HttpHeaders.CONTENT_TYPE);
            if (StrUtil.isNotBlank(requestHeader)) {
                if (requestHeader.equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)
                        || requestHeader.equalsIgnoreCase(MediaType.APPLICATION_JSON_UTF8_VALUE)) {
                    requestWrapper = new XssHttpServletRequestWrapper((HttpServletRequest) request);
                }
            }
        }
		if (requestWrapper == null) {
			chain.doFilter(request, response);
		} else {
			chain.doFilter(requestWrapper, response);
		}
	}

	@Override
	public void destroy() {

	}

}
