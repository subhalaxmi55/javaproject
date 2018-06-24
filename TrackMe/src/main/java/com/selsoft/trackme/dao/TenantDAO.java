package com.selsoft.trackme.dao;

import java.util.List;

import com.selsoft.trackme.model.Tenant;

public interface TenantDAO {

	public void saveNewTenant(Tenant tenant);

	List<Tenant> findAll();

	List<Tenant> fetchTenants(String status);

}
