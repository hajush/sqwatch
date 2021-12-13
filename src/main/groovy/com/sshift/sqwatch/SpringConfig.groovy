package com.sshift.sqwatch

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableScheduling
@EnableWebMvc
class SpringConfig implements WebMvcConfigurer {
    @Override
    void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler('/images/**', '/**')
                .addResourceLocations('file:images/', 'classpath:/static/')
    }
}
