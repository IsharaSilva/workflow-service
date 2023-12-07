package com.xitricon.workflowservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xitricon.workflowservice.dto.SupplierOnboardingRequestOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SupplierOnboardingUtil {

    private final ObjectMapper objectMapper;

    public SupplierOnboardingUtil(@Qualifier("dateTimeAwareObjectMapper") final ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
    }

    public String convertToString(SupplierOnboardingRequestOutputDTO supplierOnboardingRequestOutputDTO) {
        try {
            return objectMapper.writeValueAsString(supplierOnboardingRequestOutputDTO);
        } catch (JsonProcessingException e) {
            log.error(e.getLocalizedMessage());
            return null;
        }
    }



}
