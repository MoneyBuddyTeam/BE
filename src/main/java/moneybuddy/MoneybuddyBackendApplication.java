package moneybuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MoneybuddyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneybuddyBackendApplication.class, args);
	}

}
