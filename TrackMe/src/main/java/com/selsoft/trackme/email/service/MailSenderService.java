package com.selsoft.trackme.email.service;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.selsoft.trackme.email.common.Constants;
import com.selsoft.trackme.email.common.MailResponse;

@Component("mailSenderService")
@PropertySource("classpath:emailServices.properties")
public class MailSenderService {
 
	@Autowired(required = true)
	private JavaMailSender mailSender;
	@Autowired(required = true)
	private Environment environment;

	public MailResponse sendMail(SimpleMailMessage msg) {
		MailResponse response = new MailResponse();
		try {

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
			helper.setFrom(environment.getProperty(Constants.USERNAME));
			helper.setTo(msg.getTo());
			helper.setSubject(msg.getSubject());
			mimeMessage.setContent(msg.getText(), "text/html");
			mailSender.send(mimeMessage);
			response.setStatus("Success");
			response.setErrorCode("0000");
		} catch (Exception e) {
			response.setStatus(e.getLocalizedMessage());
			response.setErrorCode("9999");
		}
		return response;
	}

}
