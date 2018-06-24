package com.selsoft.tenant.dao;

import java.util.List;
import com.selsoft.trackme.constants.TenantConstants.TENANT_STATUS;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.User;

public interface TenantDAO {

	public void saveNewTenant(Tenant tenant) throws Throwable;

	public List<Tenant> findAll();

	public List<Tenant> fetchTenants(String status);

	public Tenant getTenantByEmail(String tenantEmailId) throws Throwable;

	public User findManagerByUserId(String managerId) throws Throwable;

	public Tenant findById(String tenantId);

	public User findUserByEmail(String email);

	public String createTemporaryTokenAndLinkForUserActivation(String email);

	public void saveUser(User user) throws Throwable;

	public void deleteTenant(Tenant tenant);

	public void deleteUser(User user);

	public Tenant findTenantById(String tenantId);

	public void updateTenant(Tenant tenant);

	public void updateUserEmail(Tenant tenant);

	public List<Tenant> fetchTenantsForManager(String managerId, TENANT_STATUS tenantStatus) throws Throwable;

	public void deleteTenant(String tenantId) throws Throwable;
	
	public void saveNewServiceRequest(ServiceRequest serviceRequest) throws Throwable;
	
	public Lease getActiveLeaseDetailForTenantById(String id);

}
