package com.selsoft.trackme.pdftemplate;

public class Properties {
	
	private String address;
	private String city;
	private String state;
	private String zip;
	private String contractFrom;
	private String contractTo;
	public Properties(String address, String city, String state, String zip, String contractFrom, String contractTo) {
		super();
		this.address = address;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.contractFrom = contractFrom;
		this.contractTo = contractTo;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
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
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getContractFrom() {
		return contractFrom;
	}
	public void setContractFrom(String contractFrom) {
		this.contractFrom = contractFrom;
	}
	public String getContractTo() {
		return contractTo;
	}
	public void setContractTo(String contractTo) {
		this.contractTo = contractTo;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Properties [address=");
		builder.append(address);
		builder.append(", city=");
		builder.append(city);
		builder.append(", state=");
		builder.append(state);
		builder.append(", zip=");
		builder.append(zip);
		builder.append(", contractFrom=");
		builder.append(contractFrom);
		builder.append(", contractTo=");
		builder.append(contractTo);
		builder.append("]");
		return builder.toString();
	}


}
