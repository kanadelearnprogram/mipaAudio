package com.kanade.mipaaudio.config;

import com.mybatisflex.spring.FlexSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.kanade.mipaaudio.mapper")
public class MyBatisFlexConfig {

    // 数据库硬编码配置（纯注解，无外部文件）
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/mipaaudio?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "114514";

    /**
     * HikariCP 数据源（你指定的连接池）
     */
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DRIVER);
        config.setJdbcUrl(URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);

        // Hikari 基础配置
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        return new HikariDataSource(config);
    }

    /**
     * MyBatis-Flex 会话工厂（自动驼峰映射）
     */
    @Bean
    public FlexSqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) {
        FlexSqlSessionFactoryBean factory = new FlexSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        return factory;
    }

    /**
     * 事务管理器（必须配置，考核核心）
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
