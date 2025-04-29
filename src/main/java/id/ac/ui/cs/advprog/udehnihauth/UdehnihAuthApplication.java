package id.ac.ui.cs.advprog.udehnihauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UdehnihAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(UdehnihAuthApplication.class, args);
	}

}