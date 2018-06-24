package com.selsoft.trackme.validation;

import com.selsoft.trackme.model.ValidError;

/**
 * 
 * @author Sudhansu Sekhar
 *
 */
public class UserValidation {

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	// First Name and Last Name Validation
	public ValidError nameValidation(String firstName, String lastName) {
		boolean valid = true;

		if (firstName.length() > 30 || lastName.length() > 30) {
			valid = false;
			return new ValidError("Error", "Name should be under 30 Characters");
		}

		String letterChars = "[a-zA-Z]+";
		if (!firstName.matches(letterChars) || !lastName.matches(letterChars)) {
			valid = false;
			return new ValidError("Error", "Name should contain only Characters");
		}

		return new ValidError("Success", "Names are Valid");

	}

	// Email Address Validation
	public ValidError emailValidation(String email) {
		if (!email.matches(EMAIL_PATTERN)) {
			return new ValidError("Error", "Email is not Valid");
		}

		return new ValidError("Success", "Email is Valid");

	}

	// Password Validation
	public ValidError passwordValidation(String password) {
		boolean valid = true;
		if (password.length() > 20 || password.length() < 8) {
			valid = false;
			return new ValidError("Error", "Password should be less than 20 and more than 8 characters in length.");
		}
		String upperCaseChars = "(.*[A-Z].*)";
		if (!password.matches(upperCaseChars)) {
			valid = false;
			return new ValidError("Error", "Password should contain atleast one upper case alphabet");
		}
		String lowerCaseChars = "(.*[a-z].*)";
		if (!password.matches(lowerCaseChars)) {
			valid = false;
			return new ValidError("Error", "Password should contain atleast one lower case alphabet");
		}
		String numbers = "(.*[0-9].*)";
		if (!password.matches(numbers)) {
			valid = false;
			return new ValidError("Error", "Password should contain atleast one number.");
		}
		String specialChars = "(.*[,@,#,$,*,_,&].*$)";
		if (!password.matches(specialChars)) {
			valid = false;
			return new ValidError("Error", "Password should contain atleast one special character");
		}
		return new ValidError("Success", "Password is valid.");
	}

}
