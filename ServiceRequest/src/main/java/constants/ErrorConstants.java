package com.selsoft.servierequest.constants;

import java.io.Serializable;

public class ErrorConstants implements Serializable {

	private static final long serialVersionUID = -6847182370248213611L;

	public static final String ERROR101 = "error101";
	public static final String ERROR101_MESSAGE = "Property needs to be activated before assigning to a tenant";

	public static final String ERROR102 = "error102";
	public static final String ERROR102_MESSAGEessage = "Property is occupied, cannot assign a tenant";
	public static final String ERROR103 = "error103";
	public static final String ERROR103_MESSAGE = " Property under maintenance, cannot assign a tenant";
	public static final String ERROR104 = "error104";
	public static final String Error104_Message = "Property is INACTIVE, cannot assign a tenant";
	public static final String AUTHENTICATIONERROR = "authError";
	public static final String AUTHENTICATIONERROR_MESSAGE = "Email or Password are not correct.";
	public static final String ERROR105 = "error105";
	public static final String ERRROR105_MESSAGE = "Tenant cannot be assigned to this Lease until it is active";

	public static final String ERROR106 = "error106";
	public static final String ERRROR106_MESSAGE = "LeaseType should be RENT";

	public static final String ERROR107 = "error107";
	public static final String ERRROR107_MESSAGE = "LeaseType should be LEASE";

	public static final String ERROR108 = "error108";
	public static final String ERRROR108_MESSAGE = "LeaseType should be BOTH";

	public static final String ERROR109 = "error109";
	public static final String ERRROR109_MESSAGE = "Cann't possible to create a lease";

	public static final String ERROR110 = "error110";
	public static final String ERRROR110_MESSAGE = "TransactionType shoulde be OWN";

	public static final String ERROR111 = "error111";
	public static final String ERRROR111_MESSAGE = "TransactionType shoulde be MGR";

	public static final String ERROR112 = "error112";
	public static final String ERRROR112_MESSAGE = "TransactionType shoulde be TNT";

	public static final String SUCCESS106 = "success106";
	public static final String SUCCESS106_MESSAGE = "Property is ACTIVE";

	public static final String SUCCESSUPDATEPASSWORD107 = "successupdatepassword";
	public static final String SUCCESSUPDATEPASSWORD107_MESSAGE = "Password Updated Successfully.";

	public static final String SUCCESSLOGOUT108 = "successlogout108";
	public static final String SUCCESSLOGOUT108_MESSAGE = "User Logged Out Successfully.";

}
