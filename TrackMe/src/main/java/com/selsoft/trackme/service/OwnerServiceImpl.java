package com.selsoft.trackme.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.selsoft.trackme.constants.TrackMeConstants;
import com.selsoft.trackme.dao.OwnerDao;
import com.selsoft.trackme.model.Owner;

@Service
public class OwnerServiceImpl implements OwnerService {

	@Autowired
	private OwnerDao ownerDao;

	@SuppressWarnings(TrackMeConstants.UNUSED)
	private static final Logger logger = Logger.getLogger(OwnerService.class);

	/**
	 * save the new owner
	 */

	public void saveNewOwner(Owner owner) {

		// handler method to save/update owner
		ownerDao.saveNewOwner(owner);
	}

	/**
	 * gets all the property owners from owner table
	 */
	@Override
	public List<Owner> getAllPropertyOwners() {
		return ownerDao.getAllPropertyOwners();

	}

	@Override
	public void checkStatus(Owner status) {
		// TODO Auto-generated method stub

	}

}
