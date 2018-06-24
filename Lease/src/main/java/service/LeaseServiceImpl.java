package com.selsoft.lease.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import com.selsoft.lease.dao.LeaseDAO;
import com.selsoft.trackme.constants.ErrorConstants;
import com.selsoft.trackme.exception.LeaseException;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.RentalDetail;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.ValidError;

@Service("leaseService")
@PropertySource("classpath:ErrorMsg.properties")
public class LeaseServiceImpl implements LeaseService {

	@Autowired
	private LeaseDAO leaseDAO;

	private static final Logger logger = Logger.getLogger(LeaseServiceImpl.class);

	private static final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public void validateNewLeaseData(Lease lease) throws Throwable {

		Date startDate = null, endDate = null, moveInDate = null;
		String startDateString = null, endDateString = null, moveInDateString = null;

		if (lease == null)
			throw new LeaseException("Error", "Cannot create lease, lease data not available, please try again");

		if (StringUtils.isBlank(lease.getPropertyId()))
			throw new LeaseException("Error", "Cannot create lease, property data not available, please try again");
		if (StringUtils.isBlank(lease.getManagerId()))
			throw new LeaseException("Error", "Cannot create lease, manager data not available, please try again");
		if (StringUtils.isBlank(lease.getTenantId()))
			throw new LeaseException("Error", "Cannot create lease, tenant data not available, please try again");

		lease.setLeaseStartDate(StringUtils.trimToEmpty(lease.getLeaseStartDate()));
		lease.setLeaseEndDate(StringUtils.trimToEmpty(lease.getLeaseEndDate()));
		lease.setMoveInDate(StringUtils.trimToEmpty(lease.getMoveInDate()));

		startDateString = lease.getLeaseStartDate();
		endDateString = lease.getLeaseEndDate();
		moveInDateString = lease.getMoveInDate();

		if (StringUtils.isBlank(startDateString))
			throw new LeaseException("Error", "Cannot create lease. Lease start date not available, please try again");
		if (StringUtils.isBlank(endDateString))
			throw new LeaseException("Error", "Cannot create lease. Lease end date not available, please try again");
		if (StringUtils.isBlank(lease.getTenure()))
			throw new LeaseException("Error", "Cannot create lease. Lease duration not available, please try again");

		try {
			if (!isValidDateFormat(startDateString)) {
				throw new LeaseException("Error",
						"Cannot create lease, invalid lease start date format " + startDateString);
			}
			startDate = isoDateFormat.parse(startDateString);
		} catch (ParseException e) {
			logger.error("Cannot create lease. Invalid lease start date, please try again", e);
			throw new LeaseException("Error", "Cannot create lease. Invalid lease start date, please try again");
		}

		try {
			if (!isValidDateFormat(endDateString)) {
				throw new LeaseException("Error",
						"Cannot create lease, invalid lease end date format " + endDateString);
			}
			endDate = isoDateFormat.parse(endDateString);
		} catch (ParseException e) {
			logger.error("Cannot create lease. Invalid lease end date, please try again", e);
			throw new LeaseException("Error", "Cannot create lease. Invalid lease end date, please try again");
		}

		if (StringUtils.isNotBlank(moveInDateString)) {
			try {
				if (!isValidDateFormat(moveInDateString)) {
					throw new LeaseException("Error",
							"Cannot create lease, invalid move in date format " + moveInDateString);
				}
				moveInDate = isoDateFormat.parse(moveInDateString);
				if (endDate.after(moveInDate)) {
					throw new LeaseException("Error",
							"Cannot create lease, move in date should be before lease end date");
				}
			} catch (ParseException e) {
				logger.error("Cannot create lease. Invalid move in date, please try again", e);
				throw new LeaseException("Error", "Cannot create lease. Invalid move in date, please try again");
			}
		}

		if (startDate.after(endDate)) {
			throw new LeaseException("Error",
					"Cannot create lease. Lease start date is later than end date, please try again");
		} else if (startDate.equals(endDate)) {
			throw new LeaseException("Error",
					"Cannot create lease. Lease start date and end date are equal, please try again");
		}

		Property property = leaseDAO.getPropertyData(lease);
		if (property == null)
			throw new LeaseException("Error", "Cannot create lease, property data not valid, please try again");
		if (!StringUtils.equals(property.getPropertyStatus(), "Active")) {
			throw new LeaseException("Error", "Cannot create lease, property status not valid "
					+ property.getPropertyStatus() + ". Please use an Active property");
		}

		lease.setOwnerId(property.getOwnerId());
		lease.setOwnerFirstName(property.getOwnerFirstName());
		lease.setOwnerLastName(property.getOwnerLastName());

		if (lease.getRent() <= 0)
			throw new LeaseException("Error", "Cannot create lease. Invalid rent, please try again");
		if (lease.getDeposit() < 0)
			throw new LeaseException("Error", "Cannot create lease. Invalid deposit, please try again");

	}

	private boolean isValidDateFormat(String date) throws Throwable {
		final String regex = "^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)";

		return Pattern.compile(regex).matcher(date).find();
	}

	@Override
	public Lease createLease(Lease lease) throws Throwable {
		lease.setLeaseStatus("Active");
		validateNewLeaseData(lease);
		leaseDAO.createLease(lease);

		Property property = new Property();
		property.setPropertyId(lease.getPropertyId());
		property.setPropertyStatus("Occupied");
		leaseDAO.updatePropertyStatus(property);

		Tenant tenant = new Tenant();
		tenant.setTenantId(lease.getTenantId());
		tenant.setTenantStatus("Active");
		leaseDAO.updateTenantStatus(tenant);

		return lease;
	}

	@Override
	public void saveRentalDetail(RentalDetail rentalDetail, String propertyId) {
		// int propId = Integer.parseInt(propertyId);
		rentalDetail.setProperytId(propertyId);
		leaseDAO.saveRentalDetail(rentalDetail, propertyId);
	}

	@Override
	public ValidError validateNewRentalData(RentalDetail rentalDetail) {

		String leaseType = rentalDetail.getLeaseType();
		logger.info(rentalDetail.getLeaseType() + " data comes into LeaseController saveRentalDetail() for processing");

		if (StringUtils.equals("RENT", leaseType)) {
			ValidError validError = new ValidError(ErrorConstants.ERROR106, ErrorConstants.ERRROR106_MESSAGE);
			return validError;

		}

		else if (StringUtils.equals("LEASE", leaseType)) {
			ValidError validError = new ValidError(ErrorConstants.ERROR107, ErrorConstants.ERRROR107_MESSAGE);
			return validError;

		} else if (StringUtils.equals("BOTH", leaseType)) {
			ValidError validError = new ValidError(ErrorConstants.ERROR108, ErrorConstants.ERRROR108_MESSAGE);
			return validError;

		}
		return null;
	}

	@Override
	public List<RentalDetail> getAllRentalDetails(Integer propertyId) {

		return leaseDAO.getAllRentalDetails(propertyId);
	}

	@Override
	public RentalDetail getRentalDetail(Integer propertyId, String inputDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse(inputDate);
		} catch (ParseException e) {

			e.printStackTrace();
		}
		return leaseDAO.getRentalDetail(propertyId, date);
	}

	@Override
	public Lease updateLease(Lease leaseRequest) throws Throwable {
		Lease lease = null;
		String id = leaseRequest.getLeaseId();
		if (id != null) {
			lease = leaseDAO.findById(id);
		}
		if (lease == null) {
			logger.info("Lease with id " + id + " not found");
			throw new LeaseException("Error", "leaseId not found, please enter a valid lease Id");
		}
		lease.setLeaseType(leaseRequest.getLeaseType());
		lease.setLeaseStartDate(leaseRequest.getLeaseStartDate());
		lease.setLeaseEndDate(leaseRequest.getLeaseEndDate());
		lease.setMoveInDate(leaseRequest.getMoveInDate());
		lease.setTenure(leaseRequest.getTenure());
		lease.setLeaseStatus(leaseRequest.getLeaseStatus());
		lease.setRent(leaseRequest.getRent());
		lease.setDeposit(leaseRequest.getDeposit());
		lease.setPropertyName(leaseRequest.getPropertyName());
		lease.setOwnerId(leaseRequest.getOwnerId());
		lease.setOwnerFirstName(leaseRequest.getOwnerFirstName());
		lease.setOwnerLastName(leaseRequest.getOwnerLastName());
		lease.setTenantFirstName(leaseRequest.getTenantFirstName());
		lease.setTenantLastName(leaseRequest.getTenantLastName());
		lease.setAdditionalTenant(leaseRequest.getAdditionalTenant());
		return leaseDAO.updateLease(lease);
	}

	@Override
	public void deleteLease(String leaseId) throws Throwable {
		if (leaseDAO.findById(leaseId) == null) {
			logger.info("Lease with id " + leaseId + " not found");
			throw new LeaseException("Error", "leaseId not found, please enter a valid lease Id");
		}

		leaseDAO.deleteLease(leaseId);

	}
}
