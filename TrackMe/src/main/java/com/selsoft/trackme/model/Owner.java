package com.selsoft.trackme.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "OWNER")
public class Owner {
	
	@Id
	private int ownerId;
	private String ownerFirstName;
	private String ownerLastName;
	private String ownerStatus;
	private String emailId;
	private String ownerPhoneNumber;
 
	public Owner() {
		this.ownerStatus = OwnerStatus.NEW.toString();
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerFirstName() {
		return ownerFirstName;
	}

	public void setOwnerFirstName(String ownerFirstName) {
		this.ownerFirstName = ownerFirstName;
	}

	public String getOwnerLastName() {
		return ownerLastName;
	}

	public void setOwnerLastName(String ownerLastName) {
		this.ownerLastName = ownerLastName;
	}

	public String getOwnerStatus() {
		return ownerStatus;
	}

	public void setOwnerStatus(String ownerStatus) {
		this.ownerStatus = ownerStatus;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getOwnerPhoneNumber() {
		return ownerPhoneNumber;
	}

	public void setOwnerPhoneNumber(String ownerPhoneNumber) {
		this.ownerPhoneNumber = ownerPhoneNumber;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Owner [ownerId=");
		builder.append(ownerId);
		builder.append(", ownerFirstName=");
		builder.append(ownerFirstName);
		builder.append(", ownerLastName=");
		builder.append(ownerLastName);
		builder.append(", ownerStatus=");
		builder.append(ownerStatus);
		builder.append(", emailId=");
		builder.append(emailId);
		builder.append(", ownerPhoneNumber=");
		builder.append(ownerPhoneNumber);
		builder.append("]");
		return builder.toString();
	}
	
	

	}