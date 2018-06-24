package com.selsoft.tenant.email.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ComponentScan(value = "com.selsoft.tenant.email.*")
@PropertySource("classpath:emailServices.properties")

public class EmailServiceBeanConfiguration {

	@Autowired(required = true)
	private Environment environment;
 
	@Bean(name = { "mailSender", "mailSenderReference" })
	public JavaMailSender getMailSenderImpl() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(environment.getProperty("mail.host"));
		mailSender.setPort(Integer.parseInt(environment.getProperty("mail.port")));
		mailSender.setUsername(environment.getProperty("mail.userName"));
		mailSender.setPassword(environment.getProperty("mail.password"));
		mailSender.setJavaMailProperties(getMailProperties());
		return mailSender;
	}

	private Properties getMailProperties() {
		Properties javaMailProperties = new Properties();
		javaMailProperties.setProperty("mail.transport.protocol", environment.getProperty("mail.transport.protocol"));
		javaMailProperties.setProperty("mail.smtp.auth", environment.getProperty("mail.smtp.auth"));
		javaMailProperties.setProperty("mail.debug", environment.getProperty("mail.debug"));
		javaMailProperties.setProperty("mail.smtp.starttls.enable", environment.getProperty("mail.smtp.starttls.enable"));
		javaMailProperties.setProperty("mail.smtp.socketFactory.class", environment.getProperty("mail.smtp.socketFactory.class"));
		javaMailProperties.setProperty("mail.smtp.ssl.trust", environment.getProperty("mail.smtp.ssl.trust"));
		return javaMailProperties;
	}

}
