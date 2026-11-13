package com.ruoyi.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.ruoyi.web.interceptor.ExtAuthInterceptor;

@Configuration
public class ExtWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ExtAuthInterceptor extAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(extAuthInterceptor)
                .addPathPatterns("/api/ext/**")
                .excludePathPatterns(
                        "/api/ext/auth/register",
                        "/api/ext/auth/register/email",
                        "/api/ext/auth/login"
                );
    }
}
