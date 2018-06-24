package com.selsoft.trackme.service;

import java.util.List;

import com.selsoft.trackme.model.Owner;

public interface OwnerService {

	public void saveNewOwner(Owner owner);

	public List<Owner> getAllPropertyOwners();

	public void checkStatus(Owner status);

}
