package com.sc.whorl.authorization.annotate;

import com.sc.whorl.authorization.utils.SecureUtil;
import com.sc.whorl.system.common.ScException;
import com.sc.whorl.system.utils.ScUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Aspect
@Configuration
@Slf4j
public class PreAuthorizeAspect {
    @Autowired
    private BeanFactory beanFactory;

    @Around("@annotation(com.sc.whorl.authorization.annotate.WhorlAuthorize) && execution(!static * *(..)) &&" +
            "(@within(org.springframework.stereotype.Controller) || " +
            "@within(org.springframework.web.bind.annotation.RestController))")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        WhorlAuthorize limitAnnotation = method.getAnnotation(WhorlAuthorize.class);
        final String value = limitAnnotation.value();
        StandardEvaluationContext simpleContext = new StandardEvaluationContext();
        simpleContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        ExpressionParser parser = new SpelExpressionParser();
        final Boolean aBoolean = parser.parseExpression(value).getValue(simpleContext, Boolean.class);
        if (!aBoolean) {
            HttpServletRequest request = ScUtils.getRequest();
            log.warn("权限认证失败,请求接口：{}，请求IP：{}，请求用户：{},访问权限:{}", request.getRequestURI(), ScUtils.getIP(request), JSONUtil.toJsonStr(SecureUtil.getUserLoginName()), value);
            throw new ScException("主机IP[" + InetAddress.getLocalHost().getHostAddress() + "]拒绝访问,没有访问权限:" + value);
        }
        return pjp.proceed();
    }


}