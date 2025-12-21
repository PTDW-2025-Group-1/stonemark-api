package pt.estga.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {
		"pt.estga.boot", "pt.estga.file", "pt.estga.chatbots"
})
public class StonemarkApplication {

	public static void main(String[] args) {
		SpringApplication.run(StonemarkApplication.class, args);
	}

}
