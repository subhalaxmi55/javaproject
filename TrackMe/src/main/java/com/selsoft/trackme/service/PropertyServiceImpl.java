package com.selsoft.trackme.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.selsoft.trackme.constants.TrackMeConstants;
import com.selsoft.trackme.dao.LeaseDAO;
import com.selsoft.trackme.dao.PropertyDAO;
import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.model.Owner;
import com.selsoft.trackme.model.Property;

@Service("propertyService")

public class PropertyServiceImpl implements PropertyService {

	@Autowired
	private PropertyDAO propertyDAO;
	@Autowired
	private LeaseDAO leaseDAO;

	@SuppressWarnings(TrackMeConstants.UNUSED)
	private static final Logger logger = Logger.getLogger(PropertyServiceImpl.class);

	/**
	 * save the new property to property table
	 */
	public void saveNewProperty(Property property) {

		Owner owner = propertyDAO.checkOwner(property.getOwnerId());

		if (owner != null) {
			property.setOwnerFirstName(owner.getOwnerFirstName());
			property.setOwnerLastName(owner.getOwnerLastName());
			propertyDAO.saveNewProperty(property);

		}
	}

	/**
	 * sets property as Active
	 */
	@Override
	public Errors setPropertyAsActive(Property property) {

		propertyDAO.setPropertyAsActive(property);
		return null;

	}

	@Override
	public List<Property> getAllProperties(String status) {
		return propertyDAO.getAllProperties(status);
	}

	public String getPropertyStatusById(int id) {

		return leaseDAO.getPropertyStatusById(id);
	}

}
