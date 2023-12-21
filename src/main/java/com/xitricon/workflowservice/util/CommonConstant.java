package com.xitricon.workflowservice.util;

public class CommonConstant {

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String SUPPLIER_ONBOARDING_PROCESS_ONE_ID = "supplier_onboarding_1";
	public static final String SUPPLIER_ONBOARDING_PROCESS_TWO_ID = "supplier_onboarding_2";
	public static final String SUPPLIER_ONBOARDING_PROCESS_ID = "supplier_onboarding";
	public static final String SUPPLIER_ONBOARDING_SUB_PROCESS_ONE_ID = "supplier_onboarding_sub_1";
	public static final String SUPPLIER_ONBOARDING_SUB_PROCESS_TWO_ID = "supplier_onboarding_sub_2";
	public static final String SUPPLIER_ONBOARDING_SUB_PROCESS_THREE_ID = "supplier_onboarding_sub_3";

	public static final String PROCESS_ENGINE_NAME = "supplierOnboarding";
	public static final String INVALID_TENANT_MSG = "Invalid Tenant ID ";
	public static final String TENANT_ONE_KEY = "T_1";
	public static final String TENANT_TWO_KEY = "T_2";
	public static final String TENANT_ID_KEY = "tenantId";
    public static final String TITLE = "title";
    public static final String WORKFLOW_TYPE = "workflowType";
    public static final String STATUS = "status";
    public static final String ACTIVITY_TYPE = "activityType";
    public static final String INTERIM_STATE = "interimState";
    public static final String DELETED = "deleted";
    public static final String RESUBMISSION = "resubmission";

	private CommonConstant() {
		throw new IllegalStateException("Utility class");
	}
}
