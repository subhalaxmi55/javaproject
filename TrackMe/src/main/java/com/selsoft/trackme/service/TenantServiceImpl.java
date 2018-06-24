package com.selsoft.trackme.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.selsoft.trackme.constants.TrackMeConstants;
import com.selsoft.trackme.dao.LeaseDAO;
import com.selsoft.trackme.dao.TenantDAO;
import com.selsoft.trackme.model.Tenant;

@Service("tenantService")
public class TenantServiceImpl implements TenantService {

	@Autowired
	private TenantDAO tenantDAO;
	@Autowired
	private LeaseDAO leaseDAO;

	@SuppressWarnings(TrackMeConstants.UNUSED)
	private static final Logger logger = Logger.getLogger(TenantService.class);

	/**
	 * add new tenant to the tenant table
	 */
	@Override
	public void addNewTenant(Tenant tenant) {
		tenantDAO.saveNewTenant(tenant);

	}

	/**
	 * get all the tenants from the tenant table
	 */
	@Override
	public List<Tenant> getAllTenants(String status) {
		return tenantDAO.fetchTenants(status);
	}

	/**
	 * save new tenant to the table
	 */
	@Override
	public List<Tenant> saveNewTenant() {
		return tenantDAO.findAll();
	}
	
	public String getTenantStatusById(int id) {
		return leaseDAO.getTenantStatusById(id);
	}

}
