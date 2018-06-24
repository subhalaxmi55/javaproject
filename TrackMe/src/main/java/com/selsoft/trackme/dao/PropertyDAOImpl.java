package com.selsoft.trackme.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.selsoft.trackme.constants.TrackMeConstants;
import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.model.Owner;
import com.selsoft.trackme.model.Property;
import com.selsoft.trackme.model.RentalDetail;
import com.selsoft.trackme.model.Tenant;
import com.selsoft.trackme.model.User;

@Repository
@Qualifier("propertyDAO")
public class PropertyDAOImpl implements PropertyDAO {
	@SuppressWarnings(TrackMeConstants.UNUSED)
	private static final Logger logger = Logger.getLogger(PropertyDAOImpl.class);

	@Autowired
	private MongoTemplate template;

	final String COLLECTION = "PROPERTY";

	/**
	 * save new property to property table
	 */

	public void saveNewProperty(Property property) {
		template.save(property);
		template.save(property.getRentalDetail());
	}

	@Override

	/**
	 * It checks owner based on ownerid
	 */
	public Owner checkOwner(int ownerId) {
		Query query = new Query(Criteria.where("ownerId").is(ownerId));
		List<Owner> ownerExist = template.find(query, Owner.class);
		return ownerExist.get(0);
	}

	@Override
	/**
	 * It sets property as active
	 */
	public Errors setPropertyAsActive(Property property) {
		Query query = new Query(Criteria.where("PropertyId").is(property.getPropertyId()));
		Update update = new Update();
		update.set("propertyStatus", "ACTIVE");
		template.updateFirst(query, update, Property.class);
		return null;

	}

	@Override
	public List<Property> getAllProperties(String status) {

		List<Property> propertyList = null;
		Query query = new Query(Criteria.where("propertyStatus").is(status));
		propertyList = template.find(query, Property.class);

		return propertyList;
	}

	@Override
	public List<Property> findAll() {

		return (List<Property>) template.findAll(Property.class);
	}

	@Override
	public void updateProperty(RentalDetail rentalDetail, int propertyId) {
		Query query = new Query(Criteria.where("propertyId").is(propertyId));
		Update update = new Update();
		update.set("rentalDetail", rentalDetail);
		template.updateFirst(query, update, Property.class);
	}

}
