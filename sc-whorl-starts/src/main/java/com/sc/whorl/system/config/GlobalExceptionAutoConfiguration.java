package com.sc.whorl.system.config;


import com.sc.whorl.system.common.RT;
import com.sc.whorl.system.common.ScException;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Configuration
@ControllerAdvice
@RestControllerAdvice
public class GlobalExceptionAutoConfiguration {


    @ExceptionHandler(ScException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RT<?> rcexception(ScException e) {
        log.info("保存业务异常信息 ex=" + e.getMessage(), e);

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        response.setHeader("resultCode", String.valueOf(e.getResultCode()));
        return RT.error(e.getResultCode(), e.getMessage());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public RT<?> validateException(ConstraintViolationException e) {
        log.error("保存全局ConstraintViolationException异常信息 ex=" + e.getMessage(), e);
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<?> cv : e.getConstraintViolations()) {
            sb.append(cv.getMessage()).append(",");
        }
        Optional<ServletRequestAttributes> servletRequestAttributes = Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        Optional<HttpServletResponse> response = Optional.ofNullable(servletRequestAttributes.get().getResponse());
        response.get().setHeader("resultCode", String.valueOf(RT.PARAM_VALID_ERROR));
        return RT.error(RT.PARAM_VALID_ERROR, sb.deleteCharAt(sb.length() - 1).toString());
    }


    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RT<?> exception(RuntimeException e) {
        log.error("保存全局RuntimeException异常信息 ex=" + e.getMessage(), e);
        Optional<ServletRequestAttributes> servletRequestAttributes = Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        Optional<HttpServletResponse> response = Optional.ofNullable(servletRequestAttributes.get().getResponse());
        response.get().setHeader("resultCode", String.valueOf(RT.INTERNAL_SERVER_ERROR));
        return RT.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RT<?> exception(Exception e) {
        log.error("保存全局Exception异常信息 ex=" + e.getMessage(), e);
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        response.setHeader("resultCode", String.valueOf(RT.INTERNAL_SERVER_ERROR));
        return RT.error(e.getMessage());
    }
}
