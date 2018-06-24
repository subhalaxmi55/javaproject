package com.selsoft.user.email.test;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.selsoft.user.email.config.EmailServiceBeanConfiguration;
import com.selsoft.user.email.service.MailSenderService;

public class MailServiceTest {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ApplicationContext context = new AnnotationConfigApplicationContext(EmailServiceBeanConfiguration.class);

		System.setProperty("java.net.preferIPv4Stack", "true");
		MailSenderService service = (MailSenderService) context.getBean("mailSenderService");
		List<String> mailId = new ArrayList<>();
		mailId.add("****@gmail.com");

	}

}
