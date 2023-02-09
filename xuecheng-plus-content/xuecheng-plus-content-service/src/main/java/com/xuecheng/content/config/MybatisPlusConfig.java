package com.xuecheng.content.config;


import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <P>
 *        Mybatis-Plus 配置
 * </p>
 */

@Configuration
@MapperScan("com.xuecheng.content.mapper")
public class MybatisPlusConfig {

   //定义分页拦截器
   @Bean
   public MybatisPlusInterceptor getMybatisPlusInterceptor(){
      MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
      mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());

      return mybatisPlusInterceptor;
   }
   

}