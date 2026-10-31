package com.petcareconnect.api.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String caminho, Resource local) throws IOException {
                        Resource pedido = local.createRelative(caminho);
                        if (pedido.exists() && pedido.isReadable()) {
                            return pedido;
                        }
                        // API, Swagger e arquivos com extensao inexistentes: 404 normal
                        if (caminho.equals("api") || caminho.startsWith("api/")
                                || caminho.startsWith("swagger-ui") || caminho.startsWith("v3")
                                || caminho.contains(".")) {
                            return null;
                        }
                        // Rotas do React Router: devolve o index.html
                        Resource index = new ClassPathResource("static/index.html");
                        return index.exists() ? index : null;
                    }
                });
    }
}