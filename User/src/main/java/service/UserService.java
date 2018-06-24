package com.selsoft.user.service;

import java.util.List;
import java.util.Locale;

import org.springframework.mail.SimpleMailMessage;

import com.selsoft.trackme.exception.UserException;
import com.selsoft.trackme.models.User;
import com.selsoft.trackme.models.UserResponse;
import com.selsoft.user.dto.PasswordDto;

import com.selsoft.trackme.models.Errors;

/**
 * 
 * @author Sudhansu Sekhar
 *
 */
public interface UserService {

	public void saveUser(User user) throws Throwable;

	public User loginUser(User user) throws Throwable;

	public void createPasswordResetTokenForUser(User user, String token);

	public User findUserByEmail(String userEmail);

	public void changeUserPassword(User user, PasswordDto password) throws Throwable;

	public SimpleMailMessage constructResetTokenEmail(String contextPath, Locale locale, String token, User user);

	public Errors userLogout(String email);

	public List<User> getAllUsers(String userType);

	public User updateUser(User user) throws Throwable;

	public void validateExpiryUser(String enteredOn)  throws UserException;

	public void deleteUser(String userId) throws Throwable ;

	public UserResponse getUserDetails(User user) throws Throwable;

}
