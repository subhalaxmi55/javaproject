package com.selsoft.user.controller;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.selsoft.trackme.models.User;
import com.selsoft.user.dto.PasswordDto;
import com.selsoft.user.email.common.MailResponse;
import com.selsoft.user.email.service.MailSenderService;
import com.selsoft.trackme.constants.UserConstants;
import com.selsoft.trackme.exception.UserException;
import com.selsoft.trackme.models.Error;
import com.selsoft.trackme.models.Errors;
import com.selsoft.user.service.UserService;
import com.selsoft.user.utils.JWTTokenUtils;

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

	@Autowired
	private UserService userService;
	
	@Autowired(required = true)
	private MailSenderService mailSender;
	
	@Autowired
	private JWTTokenUtils jwtTokenUtils;

	/**
	 * This handler method is for the User Registration, This will transfer the
	 * data to Service. The User Data will be Binded to the User Object which is
	 * coming from the Client.
	 * 
	 * @param user
	 *            as binding object to hold the User's Registration Data from
	 *            the Registration Form.
	 * @return the Errors Object as JSON Object, If any Validation error occurs
	 *         for the I/P data. ======= This handler method is for the User
	 *         Registration, This will transfer the data to Service. The User
	 *         Data will be Binded to the User Object which is coming from the
	 *         Client.
	 * 
	 * @param user
	 *            as binding object to hold the User's Registration Data from
	 *            the Registration Form.
	 * @return the Errors Object as JSON Object, If any Validation error occurs
	 *         for the I/P data.
	 */
	@RequestMapping(value = "/addUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String saveUser(@RequestBody User user) {
		logger.info(user.getFirstName() + " data comes into UserController saveUser() for processing");

		// Set user type and status
		if (StringUtils.equals(UserConstants.USER_TYPE.OWNER.toString(), user.getUserType())
				|| StringUtils.equals(UserConstants.USER_TYPE.TENANT.toString(), user.getUserType())) {
			user.setUserStatus(UserConstants.USER_STATUS.NEW.toString());
		} else if (StringUtils.equals(UserConstants.USER_TYPE.MANAGER.toString(), user.getUserType())
				|| StringUtils.equals(UserConstants.USER_TYPE.MANAGER_ADMIN.toString(), user.getUserType())) {
			user.setUserStatus(UserConstants.USER_STATUS.ACTIVE.toString());
		}

		try {
			userService.saveUser(user);
		} catch (UserException e) {
			Error error = new Error(e);
			user.addError(error);
			// return user.getErrorJSON();
		} catch (Exception e) {
			Error error = new Error("Fatal", e.toString());
			user.addError(error);
			// return user.getErrorJSON();
		} catch (Throwable t) {
			Error error = new Error("Fatal", t.toString());
			user.addError(error);
			// return user.getErrorJSON();
		}
		return jwtTokenUtils.createJWTForUser(user);
	}

	/**
	 * This method gets all the user from the user table
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getAllUsers", method = RequestMethod.GET)
	public List<User> getUser(@RequestParam(name = "userType", required = false) String userType) {

		logger.info("Data retrived from UserController getUser()");
		return userService.getAllUsers(userType);
	}

	/**
	 * This method takes argument as user object,validates email and password If
	 * it is a valid user,login otherwise throws error message
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/userLogin", method = RequestMethod.POST, produces = "application/json")
	public String saveUserLogin(@RequestBody User user) throws Throwable {
		logger.info(user.getEmail() + " data comes into UserController for login Purpose");
		if (user == null || StringUtils.isBlank(user.getEmail()) || StringUtils.isBlank(user.getPassword())) {
			user.addError("Error", "User name or password missing, cannot login");
		} else {
			try {
				user = userService.loginUser(user);
				if (user.getTrialPeriodStartDate() != null && "MGR".equalsIgnoreCase(user.getUserType()))
					userService.validateExpiryUser(user.getTrialPeriodStartDate());

			} catch (UserException e) {
				user.addError(new Error(e));
			}
		}
		return jwtTokenUtils.createJWTForUser(user);
	}

	/**
	 * If a user has not login within 20 ms.,it shows an confirmation mail to
	 * resets the password
	 * 
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
	 * 
	 * @param email
	 * @param passwordDto
	 * @return
	 */
	@RequestMapping(value = "/savePassword", method = RequestMethod.POST)
	@ResponseBody
	public void savePassword(@RequestParam("email") String email, @RequestBody PasswordDto passwordDto) {

		User user = userService.findUserByEmail(email);
		try {
			userService.changeUserPassword(user, passwordDto);
		} catch (UserException e) {
			Error error = new Error(e);
			user.addError(error);
		} catch (Exception e) {
			Error error = new Error("fatal", e.toString());
			user.addError(error);
		} catch (Throwable t) {
			Error error = new Error("fatal", t.toString());
			user.addError(error);
		}
	}

	/**
	 * This method takes email as a parameter ,it will check for valid user,if
	 * it's then saves the user's password
	 * 
	 * @param email
	 * @return
	 */
	@RequestMapping(value = "/userLogout", method = RequestMethod.GET)
	public Errors logout(@RequestParam("email") String email) {
		Errors errors = userService.userLogout(email);
		logger.info(email + " Logged Out Successfully");

		return errors;

	}

	@RequestMapping(value = { "/user",
			"/me" }, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> user(Principal principal) {
		return ResponseEntity.ok(principal);
	}

	// ------------------- Update a User
	// --------------------------------------------------------

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String updateUser(@RequestBody User user) throws Throwable {
		JSONObject jsonObject = new JSONObject();

		logger.info("Updating User ");

		try {
			userService.updateUser(user);
			jsonObject.put("success", "true");
		} catch (UserException e) {
			jsonObject.put("success", false);
			user.addError(new Error(e));
		} catch (Exception e) {
			jsonObject.put("success", false);
			user.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			user.addError(new Error("Fatal", t.toString()));
		}
		return jsonObject.toString();
	}
	
	
	@RequestMapping(value = "/deleteUser", method = RequestMethod.DELETE)
	public String deleteUser(@RequestParam("userId") String userId) throws Throwable {

		JSONObject jsonObject = new JSONObject();
		logger.info("deleting user ");

		try {
			userService.deleteUser(userId);
			jsonObject.put("sucess", "true");
			jsonObject.put("message", "user id  deleted successfully");

		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("message", "user id  does not exist");
		}
		return jsonObject.toString();
	}
	
}
