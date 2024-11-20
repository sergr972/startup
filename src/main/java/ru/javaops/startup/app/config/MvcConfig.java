package ru.javaops.startup.app.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;

import java.util.Properties;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@EnableAutoConfiguration
@Configuration
// http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-spring-mvc-auto-configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("file:./resources/static/");
        registry.setOrder(LOWEST_PRECEDENCE);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

    @Bean
    //  http://www.codejava.net/frameworks/spring/spring-mvc-url-based-view-resolution-with-urlfilenameviewcontroller-example
    public SimpleUrlHandlerMapping getUrlHandlerMapping() {
        return new SimpleUrlHandlerMapping() {{
            setMappings(new Properties() {{
                put("/view/**", new UrlFilenameViewController());
            }});
        }};
    }
}
