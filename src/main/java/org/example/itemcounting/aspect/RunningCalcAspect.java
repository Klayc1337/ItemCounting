package org.example.itemcounting.aspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RunningCalcAspect {
    @Around("execution(* org.example.itemcounting.business.service..*.*(..)) || " +
            "execution(* org.example.itemcounting.repository..*.*(..))")
    public Object running(ProceedingJoinPoint joinPoint) throws Throwable {

        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {

            long duration = System.currentTimeMillis() - start;

            log.info("{} {} -> {} ms", className, methodName, duration);
        }
    }
}
