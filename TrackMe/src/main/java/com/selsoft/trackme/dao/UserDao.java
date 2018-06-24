package com.selsoft.trackme.dao;

import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.model.PasswordResetToken;
import com.selsoft.trackme.model.User;

/**
 * 
 * @author Sudhansu Sekhar
 *
 */ 
public interface UserDao {
	public void saveUser(User user);

	public void saveUserLogin(User user);

	public User checkUserLogin(User user);

	public User findUserByEmail(String email);

	public void changeUserPassword(User user, String password);

	public void saveResetPasswordToken(PasswordResetToken token);

	public void userLogout(String email);

	public Errors saveUserType(User user, String userType);
}
