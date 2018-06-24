package com.selsoft.lease.service;

import java.util.List;

import com.selsoft.trackme.exception.LeaseException;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.RentalDetail;
import com.selsoft.trackme.models.ValidError;

public interface LeaseService {

	public Lease createLease(Lease lease) throws Throwable;

	public void saveRentalDetail(RentalDetail rentalDetail, String propertyId);

	public ValidError validateNewRentalData(RentalDetail rentalDetail);

	public List<RentalDetail> getAllRentalDetails(Integer propertyId);

	public RentalDetail getRentalDetail(Integer propertyId, String inputDate);

	public Lease updateLease(Lease lease) throws Throwable;

	public void deleteLease(String leaseId) throws Throwable;

}