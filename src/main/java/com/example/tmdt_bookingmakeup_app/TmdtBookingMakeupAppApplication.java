package com.example.tmdt_bookingmakeup_app;

import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class TmdtBookingMakeupAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmdtBookingMakeupAppApplication.class, args);
    }

}
