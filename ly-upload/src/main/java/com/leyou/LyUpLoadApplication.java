package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LyUpLoadApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyUpLoadApplication.class);
    }
}
