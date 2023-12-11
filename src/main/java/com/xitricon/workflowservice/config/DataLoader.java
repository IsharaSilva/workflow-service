package com.xitricon.workflowservice.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.xitricon.workflowservice.model.WorkflowActiveStatus;
import com.xitricon.workflowservice.repository.WorkflowActiveStatusRepository;
import com.xitricon.workflowservice.util.CommonConstant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DataLoader {
	@Bean
	CommandLineRunner dataInitializer(WorkflowActiveStatusRepository workflowActiveStatusRepository) {
		return args -> {

			List<WorkflowActiveStatus> existingWorkflowActiveStatuses = workflowActiveStatusRepository.findAll();

			if (!existingWorkflowActiveStatuses.isEmpty()) {
				log.info("Workflow active status data exists");
				return;
			}

			List<WorkflowActiveStatus> workflowActiveStatusesToSave = List.of(
					WorkflowActiveStatus.builder()
							.processDefinitionKey(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ONE_ID)
							.tenantId(CommonConstant.TENANT_ONE_KEY).active(true).build(),
					WorkflowActiveStatus.builder()
							.processDefinitionKey(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_TWO_ID)
							.tenantId(CommonConstant.TENANT_ONE_KEY).active(false).build(),
					WorkflowActiveStatus.builder().processDefinitionKey(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ID)
							.tenantId(CommonConstant.TENANT_ONE_KEY).active(false).build(),
					WorkflowActiveStatus.builder()
							.processDefinitionKey(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ONE_ID)
							.tenantId(CommonConstant.TENANT_TWO_KEY).active(false).build(),
					WorkflowActiveStatus.builder()
							.processDefinitionKey(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_TWO_ID)
							.tenantId(CommonConstant.TENANT_TWO_KEY).active(true).build(),
					WorkflowActiveStatus.builder().processDefinitionKey(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ID)
							.tenantId(CommonConstant.TENANT_TWO_KEY).active(false).build()

			);

			workflowActiveStatusRepository.saveAll(workflowActiveStatusesToSave);

			log.info("Workflow active statuses are saved");

		};
	}

}
