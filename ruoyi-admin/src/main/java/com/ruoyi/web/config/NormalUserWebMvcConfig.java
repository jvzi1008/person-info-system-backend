package com.ruoyi.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.ruoyi.web.interceptor.NormalUserIpInterceptor;

/**
 * 普通用户 IP 校验拦截器注册
 */
@Configuration
public class NormalUserWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private NormalUserIpInterceptor normalUserIpInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(normalUserIpInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/captchaImage",
                        "/logout",
                        "/error",
                        "/favicon.ico",
                        "/*.html",
                        "/profile/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/druid/**",
                        // 未授权 IP 也要能查看/提交自己的 IP，否则换网络后无法自助申请
                        "/compare/my/ip/**"
                );
    }
}
