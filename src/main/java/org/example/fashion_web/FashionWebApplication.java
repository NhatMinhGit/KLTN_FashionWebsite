package org.example.fashion_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "org.example.fashion_web")
public class FashionWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(FashionWebApplication.class, args);
	}

}
