package sc.whorl.system.extend.webhandler.handler;


import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import sc.whorl.system.extend.webhandler.SessionScope;
import sc.whorl.system.utils.StringPool;

@Slf4j
public class SessionScopeMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(SessionScope.class)) {
            return true;
        } else if (parameter.getMethodAnnotation(SessionScope.class) != null) {
            return true;
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        String annoVal = null;

        if(parameter.getParameterAnnotation(SessionScope.class)!=null){
            log.debug("param anno val::::"+parameter.getParameterAnnotation(SessionScope.class).value());
            annoVal = parameter.getParameterAnnotation(SessionScope.class).value();
        }else if(parameter.getMethodAnnotation(SessionScope.class)!=null){
            log.debug("method anno val::::"+parameter.getMethodAnnotation(SessionScope.class).value());
            annoVal = parameter.getMethodAnnotation(SessionScope.class) != null ? StrUtil.toString(parameter.getMethodAnnotation(SessionScope.class).value()) : StringPool.EMPTY;
        }
        if (webRequest.getAttribute(annoVal, RequestAttributes.SCOPE_SESSION) != null){
            return webRequest.getAttribute(annoVal, RequestAttributes.SCOPE_SESSION);
        } else {
            return null;
        }
    }
}
