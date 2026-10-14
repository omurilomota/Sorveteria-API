package com.sorveteria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI sorveteriaOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Sorveteria API")
						.description("API de gerenciamento de pedidos, produtos, sabores e usuários")
						.version("v1"));
	}
	
}
