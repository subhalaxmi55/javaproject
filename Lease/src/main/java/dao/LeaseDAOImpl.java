package com.selsoft.lease.dao;

import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.RentalDetail;
import com.selsoft.trackme.models.Tenant;

@Repository
public class LeaseDAOImpl implements LeaseDAO {

	private static final Logger logger = Logger.getLogger(LeaseDAOImpl.class);

	@Autowired
	private MongoTemplate template;

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
		template.insert(lease);
	}

	@Override
	public void saveRentalDetail(RentalDetail rentalDetail, String propertyId) {
		template.save(rentalDetail);
		// .updateProperty(rentalDetail, propertyId);

	}

	@Override
	public List<RentalDetail> getAllRentalDetails(Integer propertyId) {
		List<RentalDetail> rentalDetailList = null;

		if (propertyId != null) {

			Query query = new Query(Criteria.where("propertyId").is(propertyId));
			rentalDetailList = template.find(query, RentalDetail.class);
		} else {

			rentalDetailList = template.findAll(RentalDetail.class);
		}

		return rentalDetailList;
	}

	@Override
	public RentalDetail getRentalDetail(Integer propertyId, Date inputDate) {

		Query query = new Query(Criteria.where("propertyId").is(propertyId));
		query.limit(10);
		query.with(new Sort(Sort.Direction.DESC, "effectiveDate"));

		List<RentalDetail> rentalDetailList = template.find(query, RentalDetail.class);
		logger.info("RECENT RENTAL DETAIL: " + rentalDetailList.get(0));

		return rentalDetailList.get(0);
	}

	private Date getDateNearest(List<Date> dates, Date targetDate) {
		Date returnDate = targetDate;

		for (Date date : dates) {
			if (date.compareTo(targetDate) <= 0 && date.compareTo(returnDate) > 0) {
				returnDate = date;
			}
		}

		return returnDate;
	}

	public Property getPropertyData(Lease lease) throws Throwable {
		Query query = new Query(
				Criteria.where("propertyId").is(lease.getPropertyId()).and("managerId").is(lease.getManagerId()));
		return template.findOne(query, Property.class);
	}

	public void updatePropertyStatus(Property property) throws Throwable {
		Query query = new Query(Criteria.where("propertyId").is(property.getPropertyId()));
		Update update = Update.update("propertyStatus", property.getPropertyStatus());
		template.updateFirst(query, update, Property.class);
	}

	public void updateTenantStatus(Tenant tenant) throws Throwable {
		Query query = new Query(Criteria.where("_id").is(tenant.getTenantId()));
		Update update = Update.update("tenantStatus", tenant.getTenantStatus());
		template.updateFirst(query, update, Tenant.class);
	}

	@Override
	public Lease findById(String leaseId) {
		Query query = new Query(Criteria.where("leaseId").is(leaseId));
		List<Lease> leaseExist = template.find(query, Lease.class);
		return leaseExist.get(0);
	}

	@Override
	public void deleteLease(String leaseId) {
		template.remove(new Query(Criteria.where("leaseId").is(leaseId)), Lease.class);
	}

	@Override
	public Lease updateLease(Lease lease) {
		BasicDBObject set = new BasicDBObject("$set", lease);
		Update update = new BasicUpdate(set);
		template.updateFirst(new Query(Criteria.where("_id").is(lease.getLeaseId())), update, Lease.class);
		return lease;
	}

	@Override
	public Lease getLeaseForProperty(String propertyId) throws Throwable {
		Query query = new Query(Criteria.where("propertyId").is(propertyId));
		return template.findOne(query, Lease.class);
	}

	@Override
	public Lease getActiveLeaseDetailForTenantById(String id) {
		return template.findOne(new Query(Criteria.where("tenantId").is(id).and("leaseStatus").is("Active")),
				Lease.class);
	}
}
