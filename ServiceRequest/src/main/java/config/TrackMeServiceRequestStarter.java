package com.selsoft.servicerequest.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * 
 * @author Sudhansu Sekhar
 *
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.selsoft.servicerequest")
public class TrackMeServiceRequestStarter extends SpringBootServletInitializer {

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(TrackMeServiceRequestStarter.class);
	}

	public static void main(String[] args) {

		SpringApplication.run(TrackMeServiceRequestStarter.class);
	}

}
