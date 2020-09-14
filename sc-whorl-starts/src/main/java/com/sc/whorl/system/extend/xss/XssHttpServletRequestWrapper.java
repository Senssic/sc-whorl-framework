package com.sc.whorl.system.extend.xss;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import cn.hutool.core.util.StrUtil;
import com.sc.whorl.system.utils.ScUtils;
import com.sc.whorl.system.utils.StringPool;


public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final static HtmlFilter HTML_FILTER = new HtmlFilter();

    private final byte[] body;

    HttpServletRequest orgRequest;

    public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        orgRequest = request;
        body = ScUtils.getRequestBytes(request);
    }


    public static HttpServletRequest getOrgRequest(HttpServletRequest request) {
        if (request instanceof XssHttpServletRequestWrapper) {
            return ((XssHttpServletRequestWrapper) request).getOrgRequest();
        }

        return request;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), "UTF-8"));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {


        if (null == super.getHeader(HttpHeaders.CONTENT_TYPE)) {
            return super.getInputStream();
        }


        if (!super.getHeader(HttpHeaders.CONTENT_TYPE).equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)
                && !super.getHeader(HttpHeaders.CONTENT_TYPE).equalsIgnoreCase(MediaType.APPLICATION_JSON_UTF8_VALUE)) {
            return super.getInputStream();
        }


        String requestStr = ScUtils.getRequestStr(orgRequest, body);
        if (StrUtil.isBlank(requestStr)) {
            return super.getInputStream();
        }

        requestStr = xssEncode(requestStr);

        final ByteArrayInputStream bis = new ByteArrayInputStream(requestStr.getBytes(StringPool.UTF_8));

        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bis.read();
            }
        };

    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(xssEncode(name));
        if (StrUtil.isNotBlank(value)) {
            value = xssEncode(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] parameters = super.getParameterValues(name);
        if (parameters == null || parameters.length == 0) {
            return null;
        }

        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = xssEncode(parameters[i]);
        }
        return parameters;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        Map<String, String[]> parameters = super.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String[] values = entry.getValue();
            for (int i = 0; i < values.length; i++) {
                values[i] = xssEncode(values[i]);
            }
            map.put(entry.getKey(), values);
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(xssEncode(name));
        if (StrUtil.isNotBlank(value)) {
            value = xssEncode(value);
        }
        return value;
    }

    private String xssEncode(String input) {
        return HTML_FILTER.filter(input);
    }


    public HttpServletRequest getOrgRequest() {
        return orgRequest;
    }

}
