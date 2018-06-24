package com.selsoft.user.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

@Configuration
@ComponentScan(value = "com.selsoft.user.config.*")
public class TrackMeVelocityConfiguration {
	
	
	/*public VelocityEngine getVelocityEngine() throws IOException {
		Properties properties = new Properties();
	    properties.load(this.getClass().getResourceAsStream("/application.properties"));
	    return new VelocityEngine(properties);
	}*/
	
	@Bean(name = {"velocityEngine"})
	public VelocityEngine velocityEngine() throws VelocityException, IOException{
		VelocityEngineFactoryBean factory = new VelocityEngineFactoryBean();
		Properties props = new Properties();
		props.put("resource.loader", "class");
		props.put("class.resource.loader.class", 
				  "org.apache.velocity.runtime.resource.loader." + 
				  "ClasspathResourceLoader");
		factory.setVelocityProperties(props);
		
		return factory.createVelocityEngine();
	}

}
