package com.supermarket.supermarket_system;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Supermarket System API", version = "1.0"))
public class SupermarketSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupermarketSystemApplication.class, args);
    }

}
