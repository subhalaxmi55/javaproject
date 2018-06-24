package com.selsoft.trackme.model;

public enum OwnerStatus {


	NEW("NEW"), ACTIVE("ACTIVE"), INACTIVE("INACTIVE");

	private final String value;
	 private OwnerStatus(final String value) {
		 this.value = value;
		 }
	 public String getValue() {
		 return value; 
		 }

}
