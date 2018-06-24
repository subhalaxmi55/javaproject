package com.selsoft.trackme.pdftemplate;

import java.util.List;


public class RentalPdf {

	private List<Activity> activityList;
	private Company company;
	private Properties properties;
	private Customer customer;
	private Statement statement;

	public RentalPdf(List<Activity> activityList, Company company, Properties properties, Customer customer,
			Statement statement) {
		super();
		this.activityList = activityList;
		this.company = company;
		this.properties = properties;
		this.customer = customer;
		this.statement = statement;
	}

	public List<Activity> getActivityList() {
		return activityList;
	}

	public void setActivityList(List<Activity> activityList) {
		this.activityList = activityList;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RentalPdf [activityList=");
		builder.append(activityList);
		builder.append(", company=");
		builder.append(company);
		builder.append(", properties=");
		builder.append(properties);
		builder.append(", customer=");
		builder.append(customer);
		builder.append(", statement=");
		builder.append(statement);
		builder.append("]");
		return builder.toString();
	}

}
