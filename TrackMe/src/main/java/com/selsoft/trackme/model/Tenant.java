package com.selsoft.trackme.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "TENANT")
public class Tenant {
	
	@Id
	private int tenantId;
	private String tenantFirstName;
	private String tenantLastName;
	private String tenantEmailId;
	private String tenantPhoneNumber;
	private String tenantStatus;

	public Tenant() {
		this.tenantStatus = TenantStatus.NEW.toString();

	}

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantFirstName() {
		return tenantFirstName;
	}

	public void setTenantFirstName(String tenantFirstName) {
		this.tenantFirstName = tenantFirstName;
	}

	public String getTenantLastName() {
		return tenantLastName;
	}

	public void setTenantLastName(String tenantLastName) {
		this.tenantLastName = tenantLastName;
	}

	public String getTenantEmailId() {
		return tenantEmailId;
	}

	public void setTenantEmailId(String tenantEmailId) {
		this.tenantEmailId = tenantEmailId;
	}

	public String getTenantPhoneNumber() {
		return tenantPhoneNumber;
	}

	public void setTenantPhoneNumber(String tenantPhoneNumber) {
		this.tenantPhoneNumber = tenantPhoneNumber;
	}

	public String getTenantStatus() {
		return tenantStatus;
	}

	public void setTenantStatus(String tenantStatus) {
		this.tenantStatus = tenantStatus;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Tenant [tenantId=");
		builder.append(tenantId);
		builder.append(", tenantFirstName=");
		builder.append(tenantFirstName);
		builder.append(", tenantLastName=");
		builder.append(tenantLastName);
		builder.append(", tenantEmailId=");
		builder.append(tenantEmailId);
		builder.append(", tenantPhoneNumber=");
		builder.append(tenantPhoneNumber);
		builder.append(", tenantStatus=");
		builder.append(tenantStatus);
		builder.append("]");
		return builder.toString();
	}

	

}
