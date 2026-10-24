package com.example.websocket_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(
    basePackages = "com.example.websocket_demo",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = {
                // Disable all controllers except WolDeviceController, AuthController, and TestController
                "com\\.example\\.websocket_demo\\.controller\\.(?!WolDeviceController|AuthController|TestController).*",
                // Disable non-WOL services
                "com\\.example\\.websocket_demo\\.service\\.(chat|product|location|helper|role).*",
                "com\\.example\\.websocket_demo\\.service\\.user\\.impl\\.UserServiceImpl.*",
                // Disable non-WOL configurations
                "com\\.example\\.websocket_demo\\.configuration\\.(WebSocketConfig|WebSocketEventListener|SwaggerConfig).*",
                // Disable WebSocket security interceptor
                "com\\.example\\.websocket_demo\\.security\\.jwt\\.WebSocketAuthInterceptor.*",
                // Disable unused mappers
                "com\\.example\\.websocket_demo\\.mapper\\.(ChatMapper.*|ProductMapper.*|LocationMapper.*)"
            }
        )
    }
)
public class WebsocketDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketDemoApplication.class, args);
	}

}

