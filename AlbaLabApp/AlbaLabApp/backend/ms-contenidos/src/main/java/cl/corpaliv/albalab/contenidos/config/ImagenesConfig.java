package cl.corpaliv.albalab.contenidos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sirve las ilustraciones (generadas, sin fotografias de personas) en /contenidos/imagenes/**. */
@Configuration
public class ImagenesConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/contenidos/imagenes/**").addResourceLocations("classpath:/static/imagenes/");
    }
}
