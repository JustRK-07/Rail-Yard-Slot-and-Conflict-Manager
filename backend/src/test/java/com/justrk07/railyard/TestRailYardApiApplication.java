package com.justrk07.railyard;

import org.springframework.boot.SpringApplication;

public class TestRailYardApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(RailYardApiApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
