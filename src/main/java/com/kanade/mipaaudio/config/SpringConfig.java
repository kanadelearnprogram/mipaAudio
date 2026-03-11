package com.kanade.mipaaudio.config;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.Executor;

@Configuration
@ComponentScan(basePackages = {"com.kanade.mipaaudio.controller", "com.kanade.mipaaudio.service", "com.kanade.mipaaudio.mapper", "com.kanade.mipaaudio.config"})
@Import({MyBatisFlexConfig.class, WebConfig.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement
@EnableAsync
public class SpringConfig {

    @Bean(name = "audioUploadExecutor")
    public Executor audioUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("audio-upload-");
        executor.setRejectedExecutionHandler((r, e) ->
                System.out.println("警告：音频上传任务队列已满，任务被拒绝"));
        executor.initialize();
        return executor;
    }
}
