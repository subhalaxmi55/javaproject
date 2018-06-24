package com.selsoft.trackme.service;

import java.util.List;

import com.selsoft.trackme.model.Tenant;

public interface TenantService {

	public void addNewTenant(Tenant tenant);

	public List<Tenant> getAllTenants(String status);
	String getTenantStatusById(int id);

	public List<Tenant> saveNewTenant();
	

}
