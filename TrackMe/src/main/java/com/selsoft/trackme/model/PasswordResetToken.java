package com.selsoft.trackme.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class PasswordResetToken {

	@SuppressWarnings("unused")
	private static final int EXPIRATION = 20;

	@Id
	private long id;
	private String token;
	@DBRef
	private User user;
	private Date expiryDate;
 
	public PasswordResetToken(String token, User user, Date expiryDate) {
		this.token = token;
		this.user = user;
		this.expiryDate = expiryDate;
	}

	public long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public User getUser() {
		return user;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

}
