package pt.estga.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StonemarkApplication {

	public static void main(String[] args) {
		SpringApplication.run(StonemarkApplication.class, args);
	}

}
