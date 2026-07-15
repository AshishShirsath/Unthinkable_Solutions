package com.ashish.rentflatmatefinder;

import com.ashish.rentflatmatefinder.config.JwtProperties;
import com.ashish.rentflatmatefinder.config.OpenAiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({JwtProperties.class, OpenAiConfig.class})
public class RentflatmatefinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentflatmatefinderApplication.class, args);
	}

}
