package com.selsoft.trackme.model;

public enum LeaseType {
	
	
	RENT("RENT"), LEASE("LEASE"), BOTH("BOTH");

	private final String value;
	 private LeaseType(final String value) {
		 this.value = value;
		 }
	 public String getValue() {
		 return value; 
		 }


}
