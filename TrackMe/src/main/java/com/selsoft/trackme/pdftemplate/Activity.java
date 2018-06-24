package com.selsoft.trackme.pdftemplate;

public class Activity {
	
	private String date;
	private String ref;
	private String desc;
	private double amount;
	public Activity(String date, String ref, String desc, double amount) {
		this.date = date;
		this.ref = ref;
		this.desc = desc;
		this.amount = amount;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getRef() {
		return ref;
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Activity [date=");
		builder.append(date);
		builder.append(", ref=");
		builder.append(ref);
		builder.append(", desc=");
		builder.append(desc);
		builder.append(", amount=");
		builder.append(amount);
		builder.append("]");
		return builder.toString();
	}
	

}
