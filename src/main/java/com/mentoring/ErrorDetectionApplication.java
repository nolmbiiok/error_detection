package com.mentoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class ErrorDetectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErrorDetectionApplication.class, args);
	}

}
