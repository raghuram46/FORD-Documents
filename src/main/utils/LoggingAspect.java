package com.ford.protech.utils;

import org.aspectj.lang.JoinPoint;

import org.aspectj.lang.annotation.AfterThrowing;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(com.ford.protech..*)" + " && !within(com.ford.protech.specifications.SpecificationBuilder)" + " && !within(com.ford.protech.lookup.LookupMapper)")
    public void inFordProtechPackage() {}

    @Before("inFordProtechPackage()")
    public void logMethodExecution(JoinPoint joinPoint) {
        logger.info("{} method is about to be executed...", joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "inFordProtechPackage()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        logger.warn("Exception occurred in method: {} with message: {}", joinPoint.getSignature().getName(), ex.getMessage());
    }
}

 