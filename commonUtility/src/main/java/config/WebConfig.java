package com.selsoft.commonutility.config;

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
@ComponentScan(basePackages = "com.selsoft.commonutility")
public class WebConfig {

}