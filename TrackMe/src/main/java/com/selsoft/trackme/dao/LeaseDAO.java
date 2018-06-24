package com.selsoft.trackme.dao;

import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.model.Lease;
import com.selsoft.trackme.model.RentalDetail;
import com.selsoft.trackme.model.User;

public interface LeaseDAO {

	String getPropertyStatusById(int id);

	String getTenantStatusById(int id);

	void createLease(Lease lease);

	void saveRentalDetail(RentalDetail rentalDetail,int propertyId);
	public Errors saveLeaseType(Lease lease, String leaseType);

}
