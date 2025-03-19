package de.fsr.mariokart_backend.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
        for (ApiType apiType : ApiType.values()) {
            for (ControllerType controllerType : ControllerType.values()) {
                final ApiType finalApiType = apiType;
                final ControllerType finalControllerType = controllerType;

                configurer.addPathPrefix(apiType.getPath() + controllerType.getPath(),
                        c -> c.isAnnotationPresent(ApiController.class) &&
                                c.getAnnotation(ApiController.class).apiType() == finalApiType &&
                                c.getAnnotation(ApiController.class).controllerType() == finalControllerType);
            }
        }
    }

}
