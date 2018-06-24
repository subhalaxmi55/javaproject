package com.selsoft.commonutility.config;

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
@ComponentScan(basePackages="com.selsoft.commonutility")
public class TrackMeCommonUtilityStarter extends SpringBootServletInitializer {
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TrackMeCommonUtilityStarter.class);
    }
	
	public static void main(String[] args) {
		
		SpringApplication.run(TrackMeCommonUtilityStarter.class);
	}

}
