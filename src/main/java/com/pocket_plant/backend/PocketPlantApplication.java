package com.pocket_plant.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PocketPlantApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocketPlantApplication.class, args);
	}

}
