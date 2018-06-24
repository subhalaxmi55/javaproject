package com.selsoft.trackme.payments.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages="com.selsoft.trackme.payments")
public class TrackMePaymentsStarter extends SpringBootServletInitializer {
	
	@Autowired
    private Environment environment;

	private static final Class<TrackMePaymentsStarter> trackMePaymentsStarterClass = TrackMePaymentsStarter.class;
	
	public static void main(String[] args) {
		SpringApplication.run(trackMePaymentsStarterClass, args);
	}
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(trackMePaymentsStarterClass);
    }
	
	/*@Bean
	public MongoTemplate getMongoTemplate() throws Exception {
		return new MongoTemplate(mongoDbFactory()); 
	}
	
	public MongoDbFactory mongoDbFactory() throws Exception {
		ServerAddress serverAddress = new ServerAddress(environment.getProperty("spring.data.mongodb.host"), Integer.parseInt(environment.getProperty("spring.data.mongodb.port")));
		MongoCredential credential = MongoCredential.createCredential(environment.getProperty("spring.data.mongodb.username"), 
																		environment.getProperty("spring.data.mongodb.authenticationDatabase"), 
																		environment.getProperty("spring.data.mongodb.password").toCharArray());
		MongoClient mongoClient = new MongoClient(serverAddress, Arrays.asList(credential));
        return new SimpleMongoDbFactory(mongoClient, environment.getProperty("spring.data.mongodb.database"));    
    }*/

}
