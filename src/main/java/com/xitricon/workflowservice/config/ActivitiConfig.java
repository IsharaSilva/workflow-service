package com.xitricon.workflowservice.config;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xitricon.workflowservice.util.CommonConstant;

@Configuration
public class ActivitiConfig {

	@Bean
	ProcessEngine initSupplierOnboardingProcessEngine(@Value("${spring.activiti.datasource.url}") String jdbcUrl,
			@Value("${spring.activiti.datasource.driver-class-name}") String driverClass,
			@Value("${spring.activiti.datasource.username}") String username,
			@Value("${spring.activiti.datasource.password}") String password) {
		ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
				.createStandaloneProcessEngineConfiguration();
		return processEngineConfiguration.setProcessEngineName(CommonConstant.PROCESS_ENGINE_NAME)
				.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE).setJdbcUrl(jdbcUrl)
				.setJdbcDriver(driverClass).setJdbcUsername(username).setJdbcPassword(password).buildProcessEngine();
	}
}
