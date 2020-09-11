package sc.whorl.system.extend.webhandler.handler;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.util.StrUtil;
import sc.whorl.system.extend.webhandler.RequestAttrParam;


public class RequestAttrHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestAttrParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        RequestAttrParam requestJsonParam = parameter.getParameterAnnotation(RequestAttrParam.class);

        final HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        String aliasName = getAlias(requestJsonParam, parameter);
        return servletRequest.getAttribute(aliasName);

    }


    private String getAlias(RequestAttrParam requestJsonParam, MethodParameter parameter) {
        String alias = requestJsonParam.value();
        if (StrUtil.isBlank(alias)) {
            alias = parameter.getParameterName();
        }
        return alias;
    }
}
