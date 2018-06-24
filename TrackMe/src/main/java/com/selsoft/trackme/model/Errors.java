package com.selsoft.trackme.model;

import java.util.List;

/**
 * 
 * @author Sudhansu Sekhar
 *
 */
public class Errors {

	private List<ValidError> error;

	public Errors() {
	}
 
	public Errors(List<ValidError> error) {
		this.error = error;
	}

	public List<ValidError> getError() {
		return error;
	}

	public void setError(List<ValidError> error) {
		this.error = error;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Errors [error=");
		builder.append(error);
		builder.append("]");
		return builder.toString();
	}

}
