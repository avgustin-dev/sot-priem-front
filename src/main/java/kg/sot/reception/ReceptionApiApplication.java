package kg.sot.reception;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ReceptionApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceptionApiApplication.class, args);
    }
}
