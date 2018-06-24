package com.selsoft.trackme.service;

import com.selsoft.trackme.model.Lease;
import com.selsoft.trackme.model.RentalDetail;
import com.selsoft.trackme.model.ValidError;

public interface LeaseService {

	ValidError validateNewLeaseData(Lease lease);


	void createLease(Lease lease);

	void saveRentalDetail(RentalDetail rentalDetail,String propertyId);

}
