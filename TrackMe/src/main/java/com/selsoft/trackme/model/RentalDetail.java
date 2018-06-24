package com.selsoft.trackme.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "RENTALDETAIL")
public class RentalDetail {
	
	@Id
	private int rentId;
	private int propertyRent;
	private int deposit;
	private String leaseType;
	private Date effectiveDate;
	public int getRentId() {
		return rentId;
	}
	public void setRentId(int rentId) {
		this.rentId = rentId;
	}
	public int getPropertyRent() {
		return propertyRent;
	}
	public void setPropertyRent(int propertyRent) {
		this.propertyRent = propertyRent;
	}
	public int getDeposit() {
		return deposit;
	}
	public void setDeposit(int deposit) {
		this.deposit = deposit;
	}
	public String getLeaseType() {
		return leaseType;
	}
	public void setLeaseType(String leaseType) {
		this.leaseType = leaseType;
	}
	public Date getEffectiveDate() {
		return effectiveDate;
	}
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RentalDetail [rentId=");
		builder.append(rentId);
		builder.append(", propertyRent=");
		builder.append(propertyRent);
		builder.append(", deposit=");
		builder.append(deposit);
		builder.append(", leaseType=");
		builder.append(leaseType);
		builder.append(", effectiveDate=");
		builder.append(effectiveDate);
		builder.append("]");
		return builder.toString();
	}
	
	
}
