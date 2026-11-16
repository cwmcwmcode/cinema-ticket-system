package com.cinema.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI3 文档配置, 访问地址: http://localhost:8082/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI cinemaOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("在线影院系统 API")
                .description("电影/排片/选座下单接口文档")
                .version("1.0.0"));
    }
}
