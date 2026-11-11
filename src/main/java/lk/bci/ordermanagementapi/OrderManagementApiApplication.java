package lk.bci.ordermanagementapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OrderManagementApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderManagementApiApplication.class, args);
    }

}
