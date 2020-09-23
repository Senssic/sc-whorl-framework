package com.sc.whorl.system.extend.xss;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.util.StrUtil;

/**
 * @author senssic
 */
public class XssFilter implements Filter {
    public List<String> excludes = new ArrayList<>();

	@Override
	public void init(FilterConfig config) throws ServletException {
        String tempExcludes = config.getInitParameter("excludes");
        if (StringUtils.isNotEmpty(tempExcludes)) {
            String[] url = tempExcludes.split(",");
            for (int i = 0; url != null && i < url.length; i++) {
                excludes.add(url[i]);
            }
        }
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
                    if (!handleExcludeURL(httpServletRequest)) {
                        requestWrapper = new XssHttpServletRequestWrapper((HttpServletRequest) request);
                    }
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


    private boolean handleExcludeURL(HttpServletRequest request) {
        if (excludes == null || excludes.isEmpty()) {
            return false;
        }
        String url = request.getServletPath();
        for (String pattern : excludes) {
            Pattern p = Pattern.compile("^" + pattern);
            Matcher m = p.matcher(url);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }

}
