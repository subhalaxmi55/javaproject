package com.selsoft.trackme.dao;

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
import com.selsoft.trackme.model.Lease;
import com.selsoft.trackme.model.Property;
import com.selsoft.trackme.model.RentalDetail;
import com.selsoft.trackme.model.Tenant;

@Repository
public class LeaseDAOImpl implements LeaseDAO {

	@SuppressWarnings(TrackMeConstants.UNUSED)
	private static final Logger logger = Logger.getLogger(LeaseDAOImpl.class);

	@Autowired
	private MongoTemplate template;
	
	@Autowired
	@Qualifier("propertyDAO")
	private PropertyDAO propertyDao;

	@Override
	public String getPropertyStatusById(int id) {
		Query query = new Query(Criteria.where("propertyId").is(id));
		Property property = template.findOne(query, Property.class);

		return property.getPropertyStatus();
	}

	public String getTenantStatusById(int id) {

		Query query = new Query(Criteria.where("tenantId").is(id));
		Tenant tenant = template.findOne(query, Tenant.class);

		return tenant.getTenantStatus();
	}

	@Override
	public void createLease(Lease lease) {
		template.save(lease);
	}

	@Override
	public void saveRentalDetail(RentalDetail rentalDetail, int propertyId) {
		template.save(rentalDetail);
		propertyDao.updateProperty(rentalDetail, propertyId);

	}

	@Override
	public Errors saveLeaseType(Lease lease, String leaseType) {

		Query query = new Query(Criteria.where("leaseType").is(lease.getLeaseType()));
		Update update = new Update();
		update.set("LeaseType ", "RENT");
		update.set("LeaseType", "LEASE");
		update.set("LeaseType", "BOTH");
		template.updateFirst(query, update, Lease.class);
		return null;

	}

}
