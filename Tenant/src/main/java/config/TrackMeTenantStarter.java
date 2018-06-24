package com.selsoft.tenant.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 
 * @author Sudhansu Sekhar
 *
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages="com.selsoft.tenant")
public class TrackMeTenantStarter extends SpringBootServletInitializer {
	
	@Autowired
    private Environment environment;
	
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TrackMeTenantStarter.class);
    }
	
	public static void main(String[] args) {
		
		SpringApplication.run(TrackMeTenantStarter.class);
	}
	
	@Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getProperty("mail.host"));
        mailSender.setPort(Integer.parseInt(environment.getProperty("mail.port")));
         
        mailSender.setUsername(environment.getProperty("mail.userName"));
        mailSender.setPassword(environment.getProperty("mail.password"));
         
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", environment.getProperty("mail.transport.protocol"));
        props.put("mail.smtp.auth", environment.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", environment.getProperty("mail.smtp.starttls.enable"));
        props.put("mail.debug", environment.getProperty("mail.debug"));
        props.put("mail.smtp.socketFactory.class", environment.getProperty("mail.smtp.socketFactory.class"));
        props.put("mail.smtp.ssl.trust", environment.getProperty("mail.smtp.ssl.trust"));
         
        return mailSender;
    }
	
}
