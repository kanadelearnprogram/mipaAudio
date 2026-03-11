package com.kanade.mipaaudio.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志记录切面
 * 使用 AOP 方式自动记录 Controller 层的请求和响应
 * 
 * @author Lenovo
 * @since 2026-03-11
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * 定义切点：匹配所有 Controller 层的方法
     */
    @Pointcut("execution(* com.kanade.mipaaudio.controller..*(..))")
    public void controllerPointcut() {
        // 切点方法，用于定义切入点
    }

    /**
     * 环绕通知：记录 Controller 方法的执行过程
     * - 记录请求信息（URL、参数、IP 等）
     * - 记录方法执行时间
     * - 记录返回结果或异常
     * 
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 方法执行异常
     */
    @Around("controllerPointcut()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取请求信息
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 2. 记录请求开始
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String methodFullName = className + "." + methodName;
        
        // 3. 构建请求日志
        Map<String, Object> requestInfo = new HashMap<>();
        if (request != null) {
            requestInfo.put("method", request.getMethod());
            requestInfo.put("url", request.getRequestURI());
            requestInfo.put("ip", getClientIp(request));
        }
        requestInfo.put("class_method", methodFullName);
        requestInfo.put("parameters", Arrays.toString(joinPoint.getArgs()));
        
        log.info("========== 请求开始 ========== ");
        log.info("【请求信息】{}", requestInfo);
        log.debug("【详细参数】className={}, methodName={}, args={}", 
                className, methodName, Arrays.deepToString(joinPoint.getArgs()));
        
        try {
            // 4. 执行目标方法
            Object result = joinPoint.proceed();
            
            // 5. 计算执行时间
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 6. 记录成功响应
            log.info("========== 请求成功 ========== ");
            log.info("【执行时间】{} ms", duration);
            log.debug("【返回结果】{}", result);
            
            return result;
            
        } catch (Throwable e) {
            // 7. 记录异常
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.error("========== 请求异常 ========== ");
            log.error("【执行时间】{} ms", duration);
            log.error("【异常信息】方法：{}, 异常：{}", methodFullName, e.getMessage());
            log.error("【异常堆栈】", e);
            
            throw e; // 重新抛出异常，让上层处理
        }
    }

    /**
     * 获取客户端真实 IP 地址
     * 考虑反向代理的情况
     * 
     * @param request HTTP 请求对象
     * @return 客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多个 IP（经过多个代理），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        
        return ip;
    }
}
