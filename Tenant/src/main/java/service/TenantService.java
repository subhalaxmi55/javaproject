package com.selsoft.tenant.service;

import java.util.List;
import com.selsoft.trackme.constants.TenantConstants.TENANT_STATUS;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.ServiceRequest;

public interface TenantService {

	public List<Tenant> getTenantsForManagerBasedOnStatus(TENANT_STATUS tenantStatus, String managerId)
			throws Throwable;

	public Tenant addNewTenant(Tenant tenant, String activationUrl) throws Throwable;

	public List<Tenant> getAllTenantsForManager(String managerId) throws Throwable;

	public List<Tenant> getAllTenants(String status);

	public void updateTenant(Tenant tenant) throws Throwable;

	public void activateUser(Tenant tenant, String activationUrl) throws Throwable;

	public ServiceRequest saveNewServiceRequest(ServiceRequest serviceRequest) throws Throwable;

	public void deleteTenant(String tenantId) throws Throwable ;

}
