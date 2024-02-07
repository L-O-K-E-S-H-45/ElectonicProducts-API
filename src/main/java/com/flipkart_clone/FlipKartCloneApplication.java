package com.flipkart_clone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlipKartCloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlipKartCloneApplication.class, args);
		
		System.out.println("Helllooooo!!!!!!");
	}

}
