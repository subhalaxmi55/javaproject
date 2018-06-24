package com.selsoft.trackme.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.selsoft.trackme.dto.PasswordDto;
import com.selsoft.trackme.email.common.MailResponse;
import com.selsoft.trackme.email.service.MailSenderService;
import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.model.Owner;
import com.selsoft.trackme.model.User;
import com.selsoft.trackme.service.UserService;
import com.selsoft.trackme.utils.UserType;

/**
 * This is the UserController for the User Registration, Login and Retriving
 * User Information. This Controller class has Handler methods for the User
 * operations.
 * 
 * @author Sudhansu Sekhar
 *
 */


@RestController
@RequestMapping(value = "/user")
public class UserController {

	private static final Logger logger = Logger.getLogger(UserController.class);
	private static UserType userType = new UserType();

	@Autowired
	private UserService userService;
	@Autowired(required = true)
	private MailSenderService mailSender;

	/**
	 * This handler method is for the User Registration, This will transfer the data
	 * to Service. The User Data will be Binded to the User Object which is coming
	 * from the Client.
	 * 
	 * @param user
	 *            as binding object to hold the User's Registration Data from the
	 *            Registration Form.
	 * @return the Errors Object as JSON Object, If any Validation error occurs for
	 *         the I/P data. ======= This handler method is for the User
	 *         Registration, This will transfer the data to Service. The User Data
	 *         will be Binded to the User Object which is coming from the Client.
	 * 
	 * @param user
	 *            as binding object to hold the User's Registration Data from the
	 *            Registration Form.
	 * @return the Errors Object as JSON Object, If any Validation error occurs for
	 *         the I/P data.
	 */
	@RequestMapping(value = "/addUser", method = RequestMethod.POST)
	public ResponseEntity<Errors> saveUser(@RequestBody User user) {
		User userWithType = null;
		logger.info(user.getFirstName() + " data comes into UserController saveUser() for processing");

		if (StringUtils.equals("OWN",user.getUserType()))
			 {
			userWithType = userType.createNewOwner(user);
		}

		else if (StringUtils.equals("MGR",user.getUserType())) {
			userWithType = userType.createNewPropertyManager(user);

		} else if (StringUtils.equals("TNT",user.getUserType())) {
			userWithType = userType.createNewTenant(user);

		}
		Errors errors = userService.saveUser(userWithType);

		return new ResponseEntity<Errors>(errors, HttpStatus.CREATED);
	}

	/**
	 * This method gets all the user from the user table
	 * @return
	 */
	@RequestMapping(value = "/getUser", method = RequestMethod.GET)
	public ResponseEntity<User> getUser() {

		logger.info("Data retrived from UserController getUser()");
		return new ResponseEntity<User>(new User(), HttpStatus.ACCEPTED);
	}

	/**
	 * This method takes argument as user object,validates email and password
	 * If it is a valid user,login otherwise throws error message
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/userLogin", method = RequestMethod.POST)
	public ResponseEntity<Errors> userLogIn(@RequestBody User user) {
		logger.info(user.getEmail() + " data comes into UserController for login Purpose");
		if (user.getEmail() == null && StringUtils.equalsIgnoreCase(user.getEmail(), ("")) && user.getPassword() == null
				&& StringUtils.equalsIgnoreCase(user.getPassword(), (""))){     
			return new ResponseEntity<Errors>(HttpStatus.BAD_REQUEST);
		}
		Errors errors = userService.saveUserLogin(user);
		return new ResponseEntity<Errors>(errors, HttpStatus.CREATED);

	}
 /**
  * If a user has not login within 20 ms.,it shows an confirmation  mail to resets the password 
  * @param request
  * @param locale
  * @param email
  * @return
  */
	@RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<MailResponse> resetPassword(HttpServletRequest request, Locale locale,
			@RequestParam("email") String email) {
		User user = userService.findUserByEmail(email);
		MailResponse response = null;
		if (user != null) {
			String token = UUID.randomUUID().toString();
			userService.createPasswordResetTokenForUser(user, token);
			response = mailSender
					.sendMail(userService.constructResetTokenEmail(getAppUrl(request), locale, token, user));
		}
		return new ResponseEntity<MailResponse>(response, HttpStatus.CREATED);

	}

	private String getAppUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri;
	}

	/**
	 * This method saves the password of valid user with encrypted password
	 * @param email
	 * @param passwordDto
	 * @return
	 */
	@RequestMapping(value = "/savePassword", method = RequestMethod.POST)
	@ResponseBody
	public Errors savePassword(@RequestParam("email") String email, @RequestBody PasswordDto passwordDto) {
		
		User user = userService.findUserByEmail(email);

		Errors errors = userService.changeUserPassword(user, passwordDto);
		return errors;
	}

	/**
	 * This method gets all records
	 * @return
	 */

/*	@RequestMapping(value = "/getRecords", method = RequestMethod.GET)
	public ResponseEntity<Owner> getAllRecords() {
		logger.info("Data retrived from OwnerController getAllRecods()");
		
		
		
		return new ResponseEntity<Owner>(new Owner(), HttpStatus.ACCEPTED);
	}*/

	
	/**
	 * This method takes email as a parameter ,it will check for valid user,if it's then saves the user's password
	 * @param email
	 * @return
	 */
	@RequestMapping(value = "/userLogout", method = RequestMethod.GET)
	public Errors logout(@RequestParam("email") String email) {
		Errors errors=userService.userLogout(email);
		logger.info(email+" Logged Out Successfully");

		return errors;

	}

}
