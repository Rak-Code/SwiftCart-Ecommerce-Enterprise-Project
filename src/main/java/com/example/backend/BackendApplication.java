package com.example.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// Load .env file
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.ignoreIfMissing()
				.load();
		
		// Set environment variables from .env file
		dotenv.entries().forEach(entry -> 
			System.setProperty(entry.getKey(), entry.getValue())
		);
		
		SpringApplication.run(BackendApplication.class, args);

	}

}
