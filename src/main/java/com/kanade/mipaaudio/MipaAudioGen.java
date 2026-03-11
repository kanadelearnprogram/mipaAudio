package com.kanade.mipaaudio;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.TemplateConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MipaAudioGen {

    public static void main(String[] args) {
        // 配置数据源（HikariCP 你的连接池）
        HikariDataSource dataSource = new HikariDataSource();
        // 你的数据库名：mipa_audio
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/mipaaudio?characterEncoding=utf-8&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        // 改成你的数据库密码
        dataSource.setPassword("114514");

        // 生成配置
        GlobalConfig globalConfig = createGlobalConfig();

        // 创建生成器
        Generator generator = new Generator(dataSource, globalConfig);

        // 执行生成
        generator.generate();
        System.out.println("✅ MipaAudio 代码生成完成！");
    }

    /**
     * 适配你的项目：com.kanade.mipaaudio + 表 t_mipa_audio
     */
    public static GlobalConfig createGlobalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();

        // 1. 你的项目根包
        globalConfig.setBasePackage("com.kanade.mipaaudio");

        // 2. 表配置：前缀 t_ + 只生成你的音频表
        //globalConfig.setTablePrefix("t_");
        globalConfig.setGenerateTable("mipaaudio");

        // 3. Entity 配置
        globalConfig.setEntityGenerateEnable(true);
        globalConfig.setEntityWithLombok(true);
        // Java21 版本
        globalConfig.setEntityJdkVersion(21);

        // 4. 开启全套生成
        globalConfig.setMapperGenerateEnable(true);
        globalConfig.setServiceGenerateEnable(true);
        globalConfig.setServiceImplGenerateEnable(true);
        globalConfig.setControllerGenerateEnable(true);

        // 自动开启 下划线→驼峰 映射（MyBatis-Flex 默认）
        return globalConfig;
    }

}
