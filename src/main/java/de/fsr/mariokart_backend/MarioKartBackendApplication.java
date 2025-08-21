package de.fsr.mariokart_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MarioKartBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarioKartBackendApplication.class, args);
	}

}
