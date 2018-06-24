package com.selsoft.trackme.pdftemplate;

public class Statement {
	private String statementDate;

	public Statement(String statementDate) {
		super();
		this.statementDate = statementDate;
	}

	public String getStatementDate() {
		return statementDate;
	}

	public void setStatementDate(String statementDate) {
		this.statementDate = statementDate;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Statement [statementDate=");
		builder.append(statementDate);
		builder.append("]");
		return builder.toString();
	}
	
	
	

}
