package com.selsoft.user.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * 
 * @author Sudhansu Sekhar
 *
 */
@SpringBootApplication(exclude=DispatcherServletAutoConfiguration.class)
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages="com.selsoft.user")
public class TrackMeUserStarter extends SpringBootServletInitializer {
	
	@Autowired
    private Environment environment;
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TrackMeUserStarter.class);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(TrackMeUserStarter.class);
	}
	
	/*@Bean
    public ServletRegistrationBean servletRegistration() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();   
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(WebConfig.class);
        dispatcherServlet.setApplicationContext(applicationContext);
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet, "/User/*");
        servletRegistrationBean.setName("foo");
        return servletRegistrationBean;
    }*/
	
	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet();
	}

	@Bean
	public ServletRegistrationBean dispatchServletRegistration() {
		ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet(), "/User/user/*");
		registration.setLoadOnStartup(1);
		registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
		return registration;
	}
	
	@Bean
    public static PropertySourcesPlaceholderConfigurer properties()
    {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        Resource[] resources = new ClassPathResource[]{new ClassPathResource("application.properties"), new ClassPathResource("emailServices.properties")};
        configurer.setLocations(resources);
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }


    @Bean
    public InternalResourceViewResolver viewResolver()
    {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
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
