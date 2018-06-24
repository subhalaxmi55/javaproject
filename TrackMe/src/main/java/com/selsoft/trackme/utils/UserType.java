package com.selsoft.trackme.utils;

import com.selsoft.trackme.model.User;

public class UserType {

	public User createNewOwner(User user) {

		user.setUserType("OWN");

		return user;

	}

	public User createNewPropertyManager(User user) {
		user.setUserType("MGR");

		return user;
	}

	public User createNewTenant(User user) {
		user.setUserType("TNT");

		return user;
	}

}
