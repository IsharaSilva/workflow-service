package com.xitricon.workflowservice.util;

public class CommonConstant {

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String SUPPLIER_ONBOARDING_PROCESS_ONE_ID = "supplier_onboarding_1";
	public static final String SUPPLIER_ONBOARDING_PROCESS_TWO_ID = "supplier_onboarding_2";
	public static final String PROCESS_ENGINE_NAME = "supplierOnboarding";
	public static final String INVALID_TENANT_MSG = "Invalid Tenant ID ";
	public static final String TENANT_ONE_KEY = "T_1";
	public static final String TENANT_TWO_KEY = "T_2";

	private CommonConstant() {
		throw new IllegalStateException("Utility class");
	}
}
