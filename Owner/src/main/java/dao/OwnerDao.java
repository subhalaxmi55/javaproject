package com.selsoft.owner.dao;


import java.util.List;

import com.selsoft.trackme.exception.OwnerException;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.models.User;

public interface OwnerDao {

	public void saveNewOwner(Owner owner) throws Throwable;

	public List<Owner> getAllPropertyOwnersForManager(String managerId) throws Throwable;

	public Owner getOwnerForEmail(String email) throws Throwable;
	
	public User findManagerByUserId(String managerId) throws Throwable;
	
	public User findEnteredByUserId(String managerId) throws Throwable;
	
	public User findUserByEmail(String emailId) throws Throwable;
	
	public String createTemporaryTokenAndLinkForUserActivation(String id);
	
	public void saveUser(User user) throws OwnerException;

	public void deleteOwner(Owner owner);
	
	public void deleteUser(User user);
	
	public Owner findOwnerById(String ownerId);
	
	public void updateOwner(Owner owner);

	public void updateUserEmail(Owner owner);

	public void deleteOwner(String ownerId);
	
	public List<Transaction> getAllTransactionsForOwner(Owner owner) throws Throwable;
}