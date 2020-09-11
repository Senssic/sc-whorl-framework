package sc.whorl.system.extend.webhandler.handler;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import sc.whorl.system.extend.webhandler.RequestHeaderJsonParam;

@Slf4j
public class RequestHeaderJsonHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver
{
    @Override
    public boolean supportsParameter(MethodParameter parameter)
    {
        return parameter.hasParameterAnnotation(RequestHeaderJsonParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception
    {
        RequestHeaderJsonParam requestJsonParam = parameter.getParameterAnnotation(RequestHeaderJsonParam.class);
        final HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        String[] aliasNames=getAlias(requestJsonParam, parameter);

        String requestHeader= servletRequest.getHeader(aliasNames[0]);

        if (StrUtil.isBlank(requestHeader)) {
            log.error("requestHeader:{}为空",aliasNames[0]);
            return null;
        }

        if (2==aliasNames.length){
            String aliasName=aliasNames[1];
            // 利用fastjson转换为对应的类型
            if ( Long.class.isAssignableFrom(parameter.getParameterType())){
                return JSONUtil.parseObj(requestHeader).getLong(aliasName);
            }else if (String.class.isAssignableFrom(parameter.getParameterType())){
                return JSONUtil.parseObj(requestHeader).getStr(aliasName);
            }else if (Integer.class.isAssignableFrom(parameter.getParameterType())){
                return JSONUtil.parseObj(requestHeader).getInt(aliasName);
            }else if (Boolean.class.isAssignableFrom(parameter.getParameterType())){
                return JSONUtil.parseObj(requestHeader).getBool(aliasName);
            }else if (Byte.class.isAssignableFrom(parameter.getParameterType())){
                return JSONUtil.parseObj(requestHeader).getByte(aliasName);
            }else if (Short.class.isAssignableFrom(parameter.getParameterType())){
                return JSONUtil.parseObj(requestHeader).getShort(aliasName);
            }else if (Double.class.isAssignableFrom(parameter.getParameterType())){
                return JSONUtil.parseObj(requestHeader).getDouble(aliasName);
            }else if (Float.class.isAssignableFrom(parameter.getParameterType())){
                return JSONUtil.parseObj(requestHeader).getFloat(aliasName);
            }  else if (BigDecimal.class.isAssignableFrom(parameter.getParameterType())){
                return JSONUtil.parseObj(requestHeader).getBigDecimal(aliasName);
            }else {
                String innerParam = JSONUtil.parseObj(requestHeader).getStr(aliasName);
                if (Collection.class.isAssignableFrom(parameter.getParameterType())) {
                    return JSONUtil.toList(JSONUtil.parseArray(innerParam), parameter.getParameterType());
                }
                return JSONUtil.toBean(innerParam, parameter.getParameterType());
            }
        }

        if (isJson(requestHeader)){
            return JSONUtil.toBean(requestHeader, parameter.getParameterType());
        }
        return requestHeader;
    }


    private boolean isJson(String headerParam){
        try{
            JSONUtil.parseObj(headerParam);
        }catch (Exception e){
            return false;
        }
        return true;
    }


    private String[] getAlias(RequestHeaderJsonParam requestJsonParam, MethodParameter parameter)
    {
        String alias = requestJsonParam.value();
        if (StrUtil.isBlank(alias))
        {
            alias = parameter.getParameterName();
            return new String[]{alias};
        }
        return StrUtil.split(alias, ".");
    }
}
