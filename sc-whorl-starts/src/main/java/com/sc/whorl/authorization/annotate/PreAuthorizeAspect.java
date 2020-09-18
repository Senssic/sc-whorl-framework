package com.sc.whorl.authorization.annotate;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 *
 */
@Aspect
@Configuration
public class PreAuthorizeAspect {


    private static final Logger logger = LoggerFactory.getLogger(PreAuthorizeAspect.class);

    @Around("@annotation(com.sc.whorl.authorization.annotate.WhorlAuthorize) && execution(!static * *(..)) &&" +
            "(@within(org.springframework.stereotype.Controller) || " +
            "@within(org.springframework.web.bind.annotation.RestController))")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        WhorlAuthorize limitAnnotation = method.getAnnotation(WhorlAuthorize.class);
        final String value = limitAnnotation.value();
        final AuthorizeEnum auth = limitAnnotation.auth();


        return pjp.proceed();
    }


}