package com.mastercard.pclo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
@EnableScheduling
public class Application implements CommandLineRunner {

	@Autowired
	private OozieJob ooziejob;

	public static void main(String[] args) {
		
		System.out.println("**********************\nCoverage Application\n**********************");
		SpringApplication.run(Application.class);
	}

	@Override
	public void run(String... args) throws Exception {
		ooziejob.getOozielist();
	}

}
