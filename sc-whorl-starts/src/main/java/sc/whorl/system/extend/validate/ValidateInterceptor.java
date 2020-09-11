package sc.whorl.system.extend.validate;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import sc.whorl.system.common.RT;
import sc.whorl.system.common.ScException;
import sc.whorl.system.utils.ScUtils;

/**
 * 通过AOP校验入参的字段
 */
@Aspect
@Slf4j
public class ValidateInterceptor {

    @Autowired
    private Validator validator;

    @Around("execution(public * *(..)) && " +
            "(@within(org.springframework.stereotype.Controller) || " +
            "@within(org.springframework.web.bind.annotation.RestController))")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        if (null != args) {
            for (Object arg : args) {
                try {
                    if (!ObjectUtil.isEmpty(arg)) {
                        ScUtils.validateWithException(validator, arg);
                    }
                } catch (Exception e) {
                    if (e instanceof ConstraintViolationException) {
                        ConstraintViolationException ve = (ConstraintViolationException) e;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (ConstraintViolation<?> cv : ve.getConstraintViolations()) {
                            stringBuilder.append(cv.getMessage()).append(",");
                        }
                        throw new ScException(RT.PARAM_VALID_ERROR, stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
                    } else {
                        throw new ScException(RT.INTERNAL_SERVER_ERROR, e);
                    }
                }
            }
        }
        return pjp.proceed();
    }

}