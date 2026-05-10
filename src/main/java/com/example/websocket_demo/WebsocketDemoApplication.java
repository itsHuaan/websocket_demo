package com.example.websocket_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WebsocketDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketDemoApplication.class, args);
	}

}
