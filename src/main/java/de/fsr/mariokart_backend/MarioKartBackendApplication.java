package de.fsr.mariokart_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 1)
@EnableAsync
public class MarioKartBackendApplication {

	static void main(String[] args) {
		SpringApplication.run(MarioKartBackendApplication.class, args);
	}

}
