package com.xitricon.workflowservice.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external-api.questionnaire-service")
public class QuestionnaireServiceProperties {

	private Map<String, String> findById;

	public Map<String, String> getFindById() {
		return findById;
	}

	public void setFindById(Map<String, String> findById) {
		this.findById = findById;
	}

}
