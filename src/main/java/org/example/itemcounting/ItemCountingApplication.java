package org.example.itemcounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ItemCountingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemCountingApplication.class, args);
    }

}
