package com.hospital.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.hospital.service..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("메서드 시작: " + joinPoint.getSignature().toShortString());
    }

    @AfterReturning(pointcut = "execution(* com.hospital.service..*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        logger.info("메서드 종료: " + joinPoint.getSignature().toShortString() + ", 결과: " + result);
    }

    @AfterThrowing(pointcut = "execution(* com.hospital.service..*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        logger.error("예외 발생: " + joinPoint.getSignature().toShortString(), ex);
    }
}