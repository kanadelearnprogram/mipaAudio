package com.kanade.mipaaudio.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan("com.kanade.mipaaudio")
@Import({MyBatisFlexConfig.class, WebConfig.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement
public class SpringConfig {
}
