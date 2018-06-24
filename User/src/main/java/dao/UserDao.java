package com.selsoft.user.dao;

import java.util.List;

import com.selsoft.trackme.exception.UserException;
import com.selsoft.trackme.models.PasswordResetToken;
import com.selsoft.trackme.models.User;

/**
 * 
 * @author Sudhansu Sekhar
 *
 */
public interface UserDao {

	public void saveUser(User user) throws UserException;

	public void saveUserLogin(User user) throws Throwable;

	public User checkUserLogin(User user) throws Throwable;

	public User findUserByEmail(String email);

	public User findUserByEmailAndType(User user);

	public String createTemporaryTokenAndLink(String id);

	public void changeUserPassword(User user);

	public String createTemporaryTokenAndLinkForUserActivation(String id);

	public void userLogout(String email);

	public void saveUserType(User user, String userType);

	public List<User> getAllUsers(String userType);

	public User checkIfResetRequestIsValid(String resetPasswordId) throws Throwable;

	public void removeTemporaryPasswordResetFields(User user);

	public User findUserByTemporaryToken(String temporaryToken) throws Throwable;

	public String getOwnerIdByEmail(String email) throws Throwable;

	public String getTenantIdByEmail(String email) throws Throwable;

	public void saveResetPasswordToken(PasswordResetToken token);

	public User findByEmail(String email);

	public User updateUser(User user);

	public void deleteUser(String userId) throws Throwable;

	public User findUserById(String userId);

	public User getUserDetails(String userId) throws Throwable;

}