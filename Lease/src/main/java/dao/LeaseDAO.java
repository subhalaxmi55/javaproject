package com.selsoft.lease.dao;

import java.util.Date;
import java.util.List;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.RentalDetail;
import com.selsoft.trackme.models.Tenant;

public interface LeaseDAO {

	public String getPropertyStatusById(int id);

	public String getTenantStatusById(int id);

	public void createLease(Lease lease);

	public void saveRentalDetail(RentalDetail rentalDetail, String propertyId);

	public List<RentalDetail> getAllRentalDetails(Integer propertyId);

	public RentalDetail getRentalDetail(Integer propertyId, Date inputDate);

	public Property getPropertyData(Lease lease) throws Throwable;

	public void updatePropertyStatus(Property property) throws Throwable;

	public void updateTenantStatus(Tenant tenant) throws Throwable;

	public Lease findById(String leaseId) throws Throwable;

	public Lease updateLease(Lease lease) throws Throwable;

	public void deleteLease(String leaseId) throws Throwable;

	public Lease getLeaseForProperty(String propertyId) throws Throwable;

	public Lease getActiveLeaseDetailForTenantById(String id);
}