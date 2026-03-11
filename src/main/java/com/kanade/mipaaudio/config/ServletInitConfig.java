package com.kanade.mipaaudio.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletException;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import java.io.IOException;

public class ServletInitConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    // 初始化时配置过滤器
    @Override
    public void onStartup(ServletContext servletContext) {
        try {
            // 注册字符编码过滤器
            CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
            characterEncodingFilter.setEncoding("UTF-8");
            characterEncodingFilter.setForceEncoding(true);  // Spring 6.x 使用 setForceEncoding
            
            var filterRegistration = servletContext.getFilterRegistration("characterEncodingFilter");
            if (filterRegistration == null) {
                var filterDynamic = servletContext.addFilter("characterEncodingFilter", characterEncodingFilter);
                if (filterDynamic != null) {
                    filterDynamic.addMappingForUrlPatterns(null, false, "/*");
                }
            }
        } catch (Exception e) {
            // 如果过滤器注册失败，记录错误但继续启动
            e.printStackTrace();
        }
        
        //super.onStartup(servletContext);
    }

    // 配置文件上传参数
    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        // 1. 编码：UTF-8
        // 2. 最大文件：512MB = 536870912 字节
        // 3. 懒加载：true
        MultipartConfigElement config = new MultipartConfigElement(
                null,               // 临时文件目录（默认即可）
                536870912,          // 最大上传文件大小
                536870912 * 2,      // 最大请求大小
                0                   // 阈值
        );
        registration.setMultipartConfig(config);
        // 开启懒加载
        registration.setInitParameter("resolveLazily", "true");
    }

    // 加载配置类
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{SpringConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    // 映射路径
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}
