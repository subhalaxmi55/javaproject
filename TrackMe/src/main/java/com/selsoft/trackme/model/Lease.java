package com.selsoft.trackme.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "LEASE")
public class Lease {
	@Id
	private int leaseId;
	private String propertyName;
	private int ownerId;
	private String ownerFirstName;
	private String ownerLastName;
	private int tenantId;
	private String tenantFirstName;
	private String tenantLastName;
	private String additionalTenant;
	private int rentalId;
	private int propertyId;
	private String leaseType;
	private Date leaseStartDate;
	private Date leaseEndDate;
	private int tenure;
	private int propertyManagerId;
	private RentalDetail rentalDetail;
	public int getLeaseId() {
		return leaseId;
	}
	public void setLeaseId(int leaseId) {
		this.leaseId = leaseId;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
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
	public String getAdditionalTenant() {
		return additionalTenant;
	}
	public void setAdditionalTenant(String additionalTenant) {
		this.additionalTenant = additionalTenant;
	}
	public int getRentalId() {
		return rentalId;
	}
	public void setRentalId(int rentalId) {
		this.rentalId = rentalId;
	}
	public int getPropertyId() {
		return propertyId;
	}
	public void setPropertyId(int propertyId) {
		this.propertyId = propertyId;
	}
	public String getLeaseType() {
		return leaseType;
	}
	public void setLeaseType(String leaseType) {
		this.leaseType = leaseType;
	}
	public Date getLeaseStartDate() {
		return leaseStartDate;
	}
	public void setLeaseStartDate(Date leaseStartDate) {
		this.leaseStartDate = leaseStartDate;
	}
	public Date getLeaseEndDate() {
		return leaseEndDate;
	}
	public void setLeaseEndDate(Date leaseEndDate) {
		this.leaseEndDate = leaseEndDate;
	}
	public int getTenure() {
		return tenure;
	}
	public void setTenure(int tenure) {
		this.tenure = tenure;
	}
	public int getPropertyManagerId() {
		return propertyManagerId;
	}
	public void setPropertyManagerId(int propertyManagerId) {
		this.propertyManagerId = propertyManagerId;
	}
	public RentalDetail getRentalDetail() {
		return rentalDetail;
	}
	public void setRentalDetail(RentalDetail rentalDetail) {
		this.rentalDetail = rentalDetail;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Lease [leaseId=");
		builder.append(leaseId);
		builder.append(", propertyName=");
		builder.append(propertyName);
		builder.append(", ownerId=");
		builder.append(ownerId);
		builder.append(", ownerFirstName=");
		builder.append(ownerFirstName);
		builder.append(", ownerLastName=");
		builder.append(ownerLastName);
		builder.append(", tenantId=");
		builder.append(tenantId);
		builder.append(", tenantFirstName=");
		builder.append(tenantFirstName);
		builder.append(", tenantLastName=");
		builder.append(tenantLastName);
		builder.append(", additionalTenant=");
		builder.append(additionalTenant);
		builder.append(", rentalId=");
		builder.append(rentalId);
		builder.append(", propertyId=");
		builder.append(propertyId);
		builder.append(", leaseType=");
		builder.append(leaseType);
		builder.append(", leaseStartDate=");
		builder.append(leaseStartDate);
		builder.append(", leaseEndDate=");
		builder.append(leaseEndDate);
		builder.append(", tenure=");
		builder.append(tenure);
		builder.append(", propertyManagerId=");
		builder.append(propertyManagerId);
		builder.append(", rentalDetail=");
		builder.append(rentalDetail);
		builder.append("]");
		return builder.toString();
	}
	
	
}
