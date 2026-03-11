package com.kanade.mipaaudio.aspect;

import com.kanade.mipaaudio.entity.Audio;
import com.kanade.mipaaudio.service.AudioService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 音频下载计数切面
 * 使用 AOP 方式统计下载次数，解耦业务逻辑
 * 
 * @author Lenovo
 * @since 2026-03-11
 */
@Slf4j
@Aspect
@Component
public class DownloadCountAspect {

    @Autowired
    private AudioService audioService;

    /**
     * 定义切点：匹配所有以 download 开头的方法
     */
    @Pointcut("execution(* com.kanade.mipaaudio.service.*.download*(..))")
    public void downloadPointcut() {
        // 切点方法，用于定义切入点
    }

    /**
     * 环绕通知：在下载方法前后执行
     * - 前置：执行下载逻辑
     * - 后置：更新下载次数
     * 
     * @param joinPoint 连接点
     * @param id 音频 ID（download 方法的第一个参数）
     * @return 下载结果
     * @throws Throwable 方法执行异常
     */
    @Around(value = "downloadPointcut() && args(id)")
    public Object updateDownloadCount(ProceedingJoinPoint joinPoint, Long id) throws Throwable {
        log.info("AOP 切面拦截到下载请求，音频 ID: {}", id);
        
        // 1. 先执行原有的下载逻辑（获取文件信息等）
        Map<String, Object> result = (Map<String, Object>) joinPoint.proceed();
        
        // 2. 如果下载成功，更新下载次数
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            try {
                // 3. 查询当前音频信息
                Audio audio = audioService.getById(id);
                
                if (audio != null && (audio.getIsDelete() == null || audio.getIsDelete() == 0)) {
                    // 4. 更新下载次数
                    Integer currentDownloadCount = audio.getDownloadCount();
                    if (currentDownloadCount == null) {
                        currentDownloadCount = 0;
                    }
                    
                    audio.setDownloadCount(currentDownloadCount + 1);
                    boolean updated = audioService.updateById(audio);
                    
                    if (updated) {
                        log.info("【AOP 切面】下载次数 +1，音频 ID: {}, 当前下载次数：{}", 
                                id, audio.getDownloadCount());
                    } else {
                        log.warn("【AOP 切面】更新下载次数失败，音频 ID: {}", id);
                    }
                }
                
            } catch (Exception e) {
                // 注意：这里不抛出异常，避免影响下载流程
                log.error("【AOP 切面】更新下载次数时发生异常", e);
                // 在结果中添加警告信息，但不影响下载
                result.put("warning", "下载次数更新失败：" + e.getMessage());
            }
        } else {
            log.warn("【AOP 切面】下载失败或返回异常，不更新下载次数");
        }
        
        // 5. 返回下载结果
        return result;
    }
}
