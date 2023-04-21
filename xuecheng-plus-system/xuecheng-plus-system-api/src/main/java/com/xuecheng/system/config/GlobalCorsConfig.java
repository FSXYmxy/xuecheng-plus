package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author 31331
 * @version 1.0
 * @description 解决跨域问题
 * @date 2023/2/9 14:19
 */


@Configuration
public class GlobalCorsConfig {

    /**
    * @description 允许跨域请求
    *
    * @return org.springframework.web.filter.CorsFilter
    * @author 31331
    * @date 2023/2/9 14:35
    */
    @Bean
    public CorsFilter getCorsConfig(){

        //跨域配置
        CorsConfiguration config = new CorsConfiguration();
        //放行所有的跨域请求，方法，请求头，跨域cookie
        config.addAllowedOrigin("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        //注册配置
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);

        //返回过滤器
        return new CorsFilter(configSource);
    }
}
