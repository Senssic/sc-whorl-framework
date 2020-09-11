package sc.whorl.system.config;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.InputStreamSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import sc.whorl.system.utils.ScUtils;


@Slf4j
@Aspect
@Configuration
public class RequestLogConfiguration {
    @Around(
            "execution(!static * *(..)) &&" +
                    "(@within(org.springframework.stereotype.Controller) || " +
                    "@within(org.springframework.web.bind.annotation.RestController))"
    )
    public Object aroundApi(ProceedingJoinPoint point) throws Throwable {
        return this.aroundProcess(point);
    }

    private Object aroundProcess(ProceedingJoinPoint point) throws Throwable {
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method method = ms.getMethod();
        Object[] args = point.getArgs();
        final Map<String, Object> paraMap = new HashMap<>(16);
        for (int i = 0; i < args.length; i++) {
            MethodParameter methodParam = ScUtils.getMethodParameter(method, i);
            PathVariable pathVariable = methodParam.getParameterAnnotation(PathVariable.class);
            if (pathVariable != null) {
                continue;
            }
            RequestBody requestBody = methodParam.getParameterAnnotation(RequestBody.class);
            Object object = args[i];
            if (object instanceof BeanPropertyBindingResult) {
                continue;
            }
            if (requestBody != null && object != null) {
                paraMap.putAll(BeanMap.create(object));
            } else {
                RequestParam requestParam = methodParam.getParameterAnnotation(RequestParam.class);
                String paraName;
                if (requestParam != null && StrUtil.isNotBlank(requestParam.value())) {
                    paraName = requestParam.value();
                } else {
                    paraName = methodParam.getParameterName();
                }
                paraMap.put(paraName, object);
            }
        }
        HttpServletRequest request = ScUtils.getRequest();
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        List<String> needRemoveKeys = new ArrayList<>(paraMap.size());
        Map<String, Object> tempMap = new HashMap<>(16);
        paraMap.forEach((key, value) -> {
            if (value instanceof HttpServletRequest) {
                needRemoveKeys.add(key);
                tempMap.putAll(((HttpServletRequest) value).getParameterMap());
            } else if (value instanceof HttpServletResponse) {
                needRemoveKeys.add(key);
            } else if (value instanceof InputStream) {
                needRemoveKeys.add(key);
            } else if (value instanceof MultipartFile) {
                String fileName = ((MultipartFile) value).getOriginalFilename();
                tempMap.put(key, fileName);
            } else if (value instanceof InputStreamSource) {
                needRemoveKeys.add(key);
            } else if (value instanceof WebRequest) {
                needRemoveKeys.add(key);
                tempMap.putAll(((WebRequest) value).getParameterMap());
            }
        });
        needRemoveKeys.forEach(paraMap::remove);
        paraMap.putAll(tempMap);
        StringBuffer headerBuffer = new StringBuffer();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            String headerValue = request.getHeader(headerName);
            if (headerValue.length() > 100) {
                headerValue = headerValue.substring(0, 100) + "...";
            }
            headerBuffer.append(headerName).append("=").append(headerValue).append(",\n");
        }
        long startNs = System.nanoTime();
        Object result = null;
        try {
            result = point.proceed();
            return result;
        } finally {
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            String rtJson = String.valueOf(JSONUtil.toJsonStr(result));
            if (rtJson.length() > 1000) {
                rtJson = rtJson.substring(0, 1000) + "...";
            }
            log.info("\n========================>>>  Request Start  <<<========================\n" +
                            "===>URL:{}:{}\n" +
                            "===>Parameters: {}\n" +
                            "===>Headers: {}\n" +
                            "===>IP: {}\n" +
                            "===>ClassMethod: {}\n" +
                            "===>Result: {}\n" +
                            "===>TookMillisSecond: {}\n" +
                            "========================>>>  Request  End   <<<========================\n",
                    requestMethod, requestURI,
                    JSONUtil.toJsonStr(paraMap),
                    JSONUtil.toJsonStr(headerBuffer.toString()),
                    ScUtils.getIP(ScUtils.getRequest()),
                    ms.getDeclaringTypeName() + "." + ms.getName(),
                    rtJson,
                    tookMs);
        }
    }
}
