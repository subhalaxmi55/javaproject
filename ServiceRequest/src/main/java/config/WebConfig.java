package com.selsoft.servicerequest.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 
 * @author Sudhansu Sekhar
 *
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.selsoft.servicerequest")
public class WebConfig {

}
