package com.selsoft.trackme.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "PROPERTY")
public class Property {

	@JsonProperty(required = true)
	@Id
	@Field("_id")
	private int propertyId;
	@JsonProperty(required = true)
	private String propertyName;
	@JsonProperty(required = true)
	private String address1;
	private String address2;
	@JsonProperty(required = true)
	private String city;
	@JsonProperty(required = true)
	private String state;
	@JsonProperty(required = true)
	private String zipCode;
	private String ownerFirstName;
	private String ownerLastName;
	@JsonProperty(required = true)
	private int ownerId;
	@JsonProperty(required = true)
	private RentalDetail rentalDetail;
	private String propertyStatus;
	
	
	
	public Property() {
		
		this.propertyStatus = PropertyStatus.ACTIVE.toString();
		
	}
	
	
	public int getPropertyId() {
		return propertyId;
	}


	public void setPropertyId(int propertyId) {
		this.propertyId = propertyId;
	}


	public String getPropertyName() {
		return propertyName;
	}


	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}


	public String getAddress1() {
		return address1;
	}


	public void setAddress1(String address1) {
		this.address1 = address1;
	}


	public String getAddress2() {
		return address2;
	}


	public void setAddress2(String address2) {
		this.address2 = address2;
	}


	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}


	public String getState() {
		return state;
	}


	public void setState(String state) {
		this.state = state;
	}


	public String getZipCode() {
		return zipCode;
	}


	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
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


	public int getOwnerId() {
		return ownerId;
	}


	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}


	public RentalDetail getRentalDetail() {
		return rentalDetail;
	}


	public void setRentalDetail(RentalDetail rentalDetail) {
		this.rentalDetail = rentalDetail;
	}


	public String getPropertyStatus() {
		return propertyStatus;
	}


	public void setPropertyStatus(String propertyStatus) {
		this.propertyStatus = propertyStatus;
	}


	@Override
	public String toString() {
		return "Property [propertyId=" + propertyId + ", propertyName=" + propertyName + ", address1=" + address1
				+ ", address2=" + address2 + ", city=" + city + ", state=" + state + ", zipCode=" + zipCode
				+ ", ownerFirstName=" + ownerFirstName + ", ownerLastName=" + ownerLastName + ", ownerId=" + ownerId
				+ ", rentalDetail=" + rentalDetail + ", propertyStatus=" + propertyStatus + "]";
	}
	
}
	