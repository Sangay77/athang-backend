package com.bfs.rma;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableAsync
@OpenAPIDefinition(
        info = @Info(
                title = "Athang API",
                version = "1.0",
                description = "API documentation for fee using RMA payment gateway"
        )
)
public class RmaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RmaApplication.class, args);
    }
}
