package com.kanade.mipaaudio.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 * 存储空间控制切面
 * 使用 AOP 方式在文件上传前检查存储空间
 * 
 * @author Lenovo
 * @since 2026-03-12
 */
@Slf4j
@Aspect
@Component
public class StorageSpaceAspect {

    // 存储空间上限（1GB）
    private static final long STORAGE_LIMIT = 1024 * 1024 * 1024 ;
    // 上传目录
    private static final String UPLOAD_DIR = "E:/codelab/mipaAudio/files/";

    /**
     * 定义切点：匹配所有上传方法
     */
    @Pointcut("execution(* com.kanade.mipaaudio.service.*.upload*(..))")
    public void uploadPointcut() {
        // 切点方法，用于定义切入点
    }

    /**
     * 环绕通知：在上传方法执行前检查存储空间
     * - 前置：检查存储空间是否充足
     * - 执行：执行原上传方法
     * - 后置：更新存储空间使用情况
     * 
     * @param joinPoint 连接点
     * @return 上传结果
     * @throws Throwable 方法执行异常
     */
    @Around("uploadPointcut()")
    public Object checkStorageSpace(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("【AOP 切面】开始检查存储空间");
        
        // 1. 获取上传的文件对象
        MultipartFile file = null;
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof MultipartFile) {
                file = (MultipartFile) arg;
                break;
            }
        }
        
        if (file == null) {
            log.warn("【AOP 切面】未找到上传文件对象，跳过空间检查");
            return joinPoint.proceed();
        }
        
        try {
            // 2. 计算文件大小
            long fileSize = file.getSize();
            log.info("【AOP 切面】上传文件大小：{} bytes", fileSize);
            
            // 3. 计算已使用空间
            long usedSpace = calculateUsedSpace(UPLOAD_DIR);
            log.info("【AOP 切面】已使用空间：{} bytes", usedSpace);
            
            // 4. 计算剩余空间
            long remainingSpace = STORAGE_LIMIT - usedSpace;
            log.info("【AOP 切面】剩余空间：{} bytes", remainingSpace);
            
            // 5. 检查空间是否充足
            if (fileSize > remainingSpace) {
                log.error("【AOP 切面】存储空间不足，需要：{} bytes，剩余：{} bytes", fileSize, remainingSpace);
                
                // 创建错误结果
                Map<String, Object> errorResult = new java.util.HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "存储空间不足，剩余空间为 " + formatSize(remainingSpace));
                return errorResult;
            }
            
            log.info("【AOP 切面】存储空间充足，开始执行上传");
            
            // 6. 执行原上传方法
            Object result = joinPoint.proceed();
            
            log.info("【AOP 切面】上传完成，存储空间检查结束");
            return result;
            
        } catch (Exception e) {
            log.error("【AOP 切面】存储空间检查异常", e);
            // 检查失败时，继续执行上传流程，避免因空间检查失败而阻塞上传
            return joinPoint.proceed();
        }
    }

    /**
     * 计算目录已使用空间
     * @param directory 目录路径
     * @return 已使用空间（字节）
     */
    private long calculateUsedSpace(String directory) {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }
        
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += calculateUsedSpace(file.getAbsolutePath());
                }
            }
        }
        return size;
    }

    /**
     * 格式化文件大小
     * @param size 大小（字节）
     * @return 格式化后的大小
     */
    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return (size / 1024) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return (size / (1024 * 1024)) + " MB";
        } else {
            return (size / (1024 * 1024 * 1024)) + " GB";
        }
    }
}