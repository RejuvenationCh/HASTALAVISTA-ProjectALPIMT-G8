package dev.outfix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OutfixApplication {

	public static void main(String[] args) {
		SpringApplication.run(OutfixApplication.class, args);
	}

}
