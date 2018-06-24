package com.selsoft.trackme.model;

public enum TenantStatus {
	
	
	NEW("NEW"), ACTIVE("ACTIVE"), INACTIVE("INACTIVE");
	
	private final String value;
	 private TenantStatus(final String value) {
		 this.value = value;
		 }
	 public String getValue() {
		 return value; 
		 }

}
