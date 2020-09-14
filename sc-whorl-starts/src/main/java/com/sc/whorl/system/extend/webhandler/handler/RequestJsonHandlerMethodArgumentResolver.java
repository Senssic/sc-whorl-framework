package com.sc.whorl.system.extend.webhandler.handler;

import org.springframework.core.MethodParameter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import com.sc.whorl.system.extend.webhandler.RequestJsonParam;
import com.sc.whorl.system.extend.webhandler.wapper.RequestBodyThreadLocalInterceptor;
import com.sc.whorl.system.utils.ScUtils;
import com.sc.whorl.system.utils.StringPool;

@Slf4j
public class RequestJsonHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver
{

    @Override
    public boolean supportsParameter(MethodParameter parameter)
    {
        return parameter.hasParameterAnnotation(RequestJsonParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception
    {
        RequestJsonParam requestJsonParam = parameter.getParameterAnnotation(RequestJsonParam.class);
        final HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        String requestBody=  getRequestBody(servletRequest.getInputStream());
        if (!servletRequest.getContentType().contains(StringPool.JSON) || StrUtil.isBlank(requestBody)) {
            log.error("请求类型错误或者RequestBody为空");
            return null;
        }
        if(requestJsonParam.current()){
            if (Collection.class.isAssignableFrom(parameter.getParameterType()))
            {
                Class clazz = ScUtils.getClassGenricType(parameter.getGenericParameterType());
                return JSONUtil.toList(JSONUtil.parseArray(requestBody), clazz);
            }
            return JSONUtil.toBean(requestBody, parameter.getParameterType());
        }
        String aliasName=getAlias(requestJsonParam,parameter);
        if ( Long.class.isAssignableFrom(parameter.getParameterType())){
            return JSONUtil.parseObj(requestBody).getLong(aliasName);
        }else if (String.class.isAssignableFrom(parameter.getParameterType())){
            return JSONUtil.parseObj(requestBody).getStr(aliasName);
        }else if (Integer.class.isAssignableFrom(parameter.getParameterType())){
            return JSONUtil.parseObj(requestBody).getInt(aliasName);
        }else if (Boolean.class.isAssignableFrom(parameter.getParameterType())){
            return JSONUtil.parseObj(requestBody).getBool(aliasName);
        }else if (Byte.class.isAssignableFrom(parameter.getParameterType())){
            return JSONUtil.parseObj(requestBody).getByte(aliasName);
        }else if (Short.class.isAssignableFrom(parameter.getParameterType())){
            return JSONUtil.parseObj(requestBody).getShort(aliasName);
        }else if (Double.class.isAssignableFrom(parameter.getParameterType())){
            return JSONUtil.parseObj(requestBody).getDouble(aliasName);
        }else if (Float.class.isAssignableFrom(parameter.getParameterType())){
            return JSONUtil.parseObj(requestBody).getFloat(aliasName);
        }else if (BigDecimal.class.isAssignableFrom(parameter.getParameterType())){
            return JSONUtil.parseObj(requestBody).getBigDecimal(aliasName);
        }else if (Date.class.isAssignableFrom(parameter.getParameterType())){
            String date = JSONUtil.parseObj(requestBody).getStr(aliasName);
            return DateUtil.parse(date, requestJsonParam.dateFormat());
        }
        else if (Collection.class.isAssignableFrom(parameter.getParameterType())){
            Class clazz = ScUtils.getClassGenricType(parameter.getGenericParameterType());
            String jsonArray = JSONUtil.parseObj(requestBody).getStr(aliasName);
            return JSONUtil.toList(JSONUtil.parseArray(jsonArray), clazz);
        }else {
            String innerParam = JSONUtil.parseObj(requestBody).getStr(aliasName);
            if (Collection.class.isAssignableFrom(parameter.getParameterType()))
            {
                Class clazz = ScUtils.getClassGenricType(parameter.getGenericParameterType());
                return JSONUtil.toList(JSONUtil.parseArray(innerParam), clazz);
            }
            return JSONUtil.toBean(innerParam, parameter.getParameterType());
        }
    }
    private String getRequestBody(InputStream inputStream) throws IOException
    {
        String requestBody= RequestBodyThreadLocalInterceptor.RequestBodyThreadLocal.get();
        if (StrUtil.isBlank(requestBody)) {
            requestBody= StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            RequestBodyThreadLocalInterceptor.RequestBodyThreadLocal.set(requestBody);
        }
        return requestBody;
    }
    private String getAlias(RequestJsonParam requestJsonParam, MethodParameter parameter)
    {
        String alias = requestJsonParam.value();
        if (StrUtil.isBlank(alias))
        {
            alias = parameter.getParameterName();
        }
        return alias;
    }
}
